document.addEventListener('DOMContentLoaded', function() {
    const refreshBtn = document.getElementById('refreshBtn');
    const graphContainer = document.getElementById('graphContainer');
    const loadingElement = document.getElementById('loading');
    const statusMessage = document.getElementById('statusMessage');
    const zoomInBtn = document.getElementById('zoomIn');
    const zoomOutBtn = document.getElementById('zoomOut');
    const resetZoomBtn = document.getElementById('resetZoom');
    
    let currentZoom = 1;
    let svg, g;
    
    loadGraphData();
    refreshBtn.addEventListener('click', function() {
        loadGraphData();
    });
    
    function loadGraphData() {
        loadingElement.style.display = 'block';
        graphContainer.innerHTML = '';
        
        fetch('/api/countries/graph-data')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load graph data');
                }
                return response.json();
            })
            .then(data => {
                loadingElement.style.display = 'none';
                const graphData = transformToGraphData(data);
                renderGraph(graphData);
            })
            .catch(error => {
                console.error('Error fetching graph data:', error);
                loadingElement.textContent = 'Error loading graph data. Please try again.';
                loadingElement.style.color = 'red';
            });
    }
    
    function transformToGraphData(countries) {
        console.log("Raw countries data:", countries);
        const nodes = countries.map(country => ({
            id: country.id,
            name: country.name,
            code: country.code,
            continentId: country.continentId,
            continentName: country.continentName,
            color: country.color
        }));
        
        const links = [];
        const maxConnectionsPerCountry = 5;
        const processedPairs = new Set();
        
        countries.forEach(country => {
            if (country.neighborIds && country.neighborIds.length > 0) {
                const connections = Math.min(country.neighborIds.length, maxConnectionsPerCountry);
                
                for (let i = 0; i < connections; i++) {
                    const neighborId = country.neighborIds[i];
                    const pairKey = [Math.min(country.id, neighborId), Math.max(country.id, neighborId)].join('-');
                    
                    if (!processedPairs.has(pairKey)) {
                        processedPairs.add(pairKey);
                        const sourceExists = nodes.some(node => node.id === country.id);
                        const targetExists = nodes.some(node => node.id === neighborId);
                        
                        if (sourceExists && targetExists) {
                            links.push({
                                source: country.id,
                                target: neighborId
                            });
                        }
                    }
                }
            }
        });
        
        console.log(`Processed ${nodes.length} nodes and ${links.length} connections`);
        return { nodes, links };
    }
    
    function renderGraph(graphData) {
        const nodes = graphData.nodes;
        const links = graphData.links;
        
        console.log("Nodes with colors:", nodes);
        
        if (nodes.length === 0) {
            showStatus('No countries found in the database', 'error');
            return;
        }
        
        const validLinks = links.filter(link => {
            const sourceExists = nodes.some(node => node.id === link.source);
            const targetExists = nodes.some(node => node.id === link.target);
            return sourceExists && targetExists;
        });
        
        const width = graphContainer.clientWidth;
        const height = graphContainer.clientHeight;
        
        const zoomControls = document.querySelector('.zoom-controls');
        if (zoomControls) {
            graphContainer.removeChild(zoomControls);
        }
        
        d3.select("#graphContainer svg").remove();
        
        graphContainer.innerHTML += `
            <div class="zoom-controls">
                <button class="zoom-button" id="zoomIn">+</button>
                <button class="zoom-button" id="zoomOut">-</button>
                <button class="zoom-button" id="resetZoom">⟲</button>
            </div>
        `;
        
        svg = d3.select("#graphContainer").append("svg")
            .attr("width", "100%")
            .attr("height", "100%")
            .attr("viewBox", `0 0 ${width} ${height}`);
        
        const zoom = d3.zoom()
            .scaleExtent([0.1, 4])
            .on("zoom", (event) => {
                g.attr("transform", event.transform);
                currentZoom = event.transform.k;
            });
        
        svg.call(zoom);
        
        g = svg.append("g")
            .attr("width", width)
            .attr("height", height);
        
        const continentGroups = {};
        nodes.forEach(node => {
            if (node.continentId) {
                if (!continentGroups[node.continentId]) {
                    continentGroups[node.continentId] = [];
                }
                continentGroups[node.continentId].push(node);
            }
        });
        
        const link = g.append("g")
            .selectAll("line")
            .data(validLinks)
            .enter().append("line")
            .attr("stroke", "#999")
            .attr("stroke-opacity", 0.6)
            .attr("stroke-width", 1.5);
        
        const node = g.append("g")
            .selectAll("circle")
            .data(nodes)
            .enter().append("circle")
            .attr("r", d => d.isCapital ? 8 : 5)
            .attr("fill", d => d.color || "#1f77b4")
            .attr("stroke", "#fff")
            .attr("stroke-width", 1.5)
            .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended));
        
        const label = g.append("g")
            .selectAll("text")
            .data(nodes)
            .enter().append("text")
            .text(d => d.name)
            .attr("font-size", "10px")
            .attr("dx", 12)
            .attr("dy", 4)
            .attr("fill", "black")
            .style("opacity", d => {
                const connectionCount = validLinks.filter(link => 
                    link.source.id === d.id || link.source === d.id || 
                    link.target.id === d.id || link.target === d.id
                ).length;
                return connectionCount > 2 ? 0.9 : 0.3;
            });
        
        node.append("title")
            .text(d => `${d.name}\nContinent: ${d.continentName || 'Unknown'}\nColor: ${d.color || 'None'}`);
        
        const simulation = d3.forceSimulation(nodes)
            .force("link", d3.forceLink(validLinks).id(d => d.id).distance(100))
            .force("charge", d3.forceManyBody().strength(-200))
            .force("center", d3.forceCenter(width / 2, height / 2))
            .force("x", d3.forceX(width / 2).strength(0.05))
            .force("y", d3.forceY(height / 2).strength(0.05))
            .force("collide", d3.forceCollide().radius(30));
        
        const isolatedNodes = nodes.filter(node => 
            !validLinks.some(link => 
                link.source.id === node.id || link.source === node.id || 
                link.target.id === node.id || link.target === node.id
            )
        );
        
        if (isolatedNodes.length > 0) {
            showStatus(`Found ${isolatedNodes.length} isolated countries with no connections. They are positioned on the edges.`, 'info');
        
            const radius = Math.min(width, height) * 0.4;
            isolatedNodes.forEach((node, i) => {
                const angle = (i / isolatedNodes.length) * 2 * Math.PI;
                node.fx = width/2 + radius * Math.cos(angle);
                node.fy = height/2 + radius * Math.sin(angle);
            });
        }
        
        simulation.on("tick", () => {
            link
                .attr("x1", d => d.source.x)
                .attr("y1", d => d.source.y)
                .attr("x2", d => d.target.x)
                .attr("y2", d => d.target.y);
            
            node
                .attr("cx", d => d.x)
                .attr("cy", d => d.y);
            
            label
                .attr("x", d => d.x)
                .attr("y", d => d.y);
        });
        
        setTimeout(() => {
            simulation.stop();
        }, 10000);
        
        document.getElementById('zoomIn').onclick = function() {
            svg.transition().duration(300).call(zoom.scaleBy, 1.3);
        };
        
        document.getElementById('zoomOut').onclick = function() {
            svg.transition().duration(300).call(zoom.scaleBy, 0.7);
        };
        
        document.getElementById('resetZoom').onclick = function() {
            svg.transition().duration(300).call(
                zoom.transform,
                d3.zoomIdentity.translate(0, 0).scale(1)
            );
        };
        
        svg.call(zoom.transform, d3.zoomIdentity.translate(width/4, height/4).scale(0.8));
        
        function dragstarted(event, d) {
            if (!event.active) simulation.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
        }
        
        function dragged(event, d) {
            d.fx = event.x;
            d.fy = event.y;
        }
        
        function dragended(event, d) {
            if (!event.active) simulation.alphaTarget(0);
            if (!isolatedNodes.includes(d)) {
                d.fx = null;
                d.fy = null;
            }
        }
        
        const actualCountries = nodes.length;
        const actualConnections = validLinks.length;
        showStatus(`Graph rendered with ${actualCountries} countries and ${actualConnections} connections`, 'success');
    }
    
    function showStatus(message, type) {
        statusMessage.textContent = message;
        statusMessage.className = 'status ' + type;
        statusMessage.style.display = 'block';
        
        setTimeout(() => {
            statusMessage.style.display = 'none';
        }, 5000);
    }
});
