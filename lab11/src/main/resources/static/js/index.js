document.addEventListener('DOMContentLoaded', function() {
    const assignColorsBtn = document.getElementById('assignColorsBtn');
    const refreshBtn = document.getElementById('refreshBtn');
    const continentsContainer = document.getElementById('continentsContainer');
    const loadingElement = document.getElementById('loading');
    const statusMessage = document.getElementById('statusMessage');
    const loginBtn = document.getElementById('loginBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const authStatus = document.getElementById('authStatus');
    
    let authToken = localStorage.getItem('jwtToken');
    updateAuthUI();
    loadCountries();
    
    loginBtn.addEventListener('click', function() {
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        
        fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Login failed');
            }
            return response.json();
        })
        .then(data => {
            authToken = data.token;
            localStorage.setItem('jwtToken', authToken);
            updateAuthUI();
            showStatus('Login successful!', 'success');
        })
        .catch(error => {
            console.error('Login error:', error);
            showStatus('Login failed. Please check your credentials.', 'error');
        });
    });
    
    logoutBtn.addEventListener('click', function() {
        authToken = null;
        localStorage.removeItem('jwtToken');
        updateAuthUI();
        showStatus('Logged out successfully', 'success');
    });
    
    function updateAuthUI() {
        if (authToken) {
            loginBtn.style.display = 'none';
            logoutBtn.style.display = 'inline-block';
            authStatus.textContent = 'Logged in';
            authStatus.className = 'auth-status logged-in';
        } else {
            loginBtn.style.display = 'inline-block';
            logoutBtn.style.display = 'none';
            authStatus.textContent = 'Not logged in';
            authStatus.className = 'auth-status logged-out';
        }
    }
    
    assignColorsBtn.addEventListener('click', function() {
        if (!authToken) {
            showStatus('Please login first to assign colors', 'error');
            return;
        }
        
        fetch('/api/countries/assign-colors', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + authToken
            }
        })
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    authToken = null;
                    localStorage.removeItem('jwtToken');
                    updateAuthUI();
                    throw new Error('Authentication expired. Please login again.');
                }
                throw new Error('Failed to assign colors');
            }
            
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return response.json();
            } else {
                return { success: true, message: 'Operation completed' };
            }
        })
        .then(data => {
            showStatus(data.message || 'Successfully assigned colors!', 'success');
            loadCountries();
        })
        .catch(error => {
            console.error('Error assigning colors:', error);
            showStatus(error.message || 'Error assigning colors. Please try again.', 'error');
        });
    });
    
    refreshBtn.addEventListener('click', function() {
        loadCountries();
    });
    
    function loadCountries() {
        loadingElement.style.display = 'block';
        continentsContainer.innerHTML = '';
        
        fetch('/api/countries/graph-data')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load countries');
                }
                return response.json();
            })
            .then(countries => {
                loadingElement.style.display = 'none';
                displayCountriesByContinent(countries);
            })
            .catch(error => {
                console.error('Error fetching countries:', error);
                loadingElement.textContent = 'Error loading countries. Please try again.';
                loadingElement.style.color = 'red';
            });
    }
    
    function displayCountriesByContinent(countries) {
        const continents = {};
        const processedCountries = new Set();
        
        countries.forEach(country => {
            let continent = 'Other';
            
            if (country.continentName && country.continentName.trim() !== '') {
                continent = country.continentName;
            }
            
            if (!continents[continent]) {
                continents[continent] = [];
            }
            if (!processedCountries.has(country.id)) {
                processedCountries.add(country.id);
                continents[continent].push(country);
            }
        });
        
        continentsContainer.innerHTML = '';
        
        Object.keys(continents).sort().forEach(continent => {
            const continentSection = document.createElement('div');
            continentSection.className = 'continent-section';
            
            const continentTitle = document.createElement('div');
            continentTitle.className = 'continent-title';
            continentTitle.textContent = continent;
            continentSection.appendChild(continentTitle);
            
            const countriesGrid = document.createElement('div');
            countriesGrid.className = 'countries-grid';
            
            continents[continent].sort((a, b) => a.name.localeCompare(b.name));
            
            continents[continent].forEach(country => {
                const countryCard = document.createElement('div');
                countryCard.className = 'country-card';
                
                const backgroundColor = country.color || '#CCCCCC';
                countryCard.style.backgroundColor = backgroundColor;
                
                let textColor = 'black';
                if (backgroundColor && backgroundColor !== '#CCCCCC') {
                    try {
                        const color = backgroundColor.replace('#', '');
                        const r = parseInt(color.substr(0, 2), 16);
                        const g = parseInt(color.substr(2, 2), 16);
                        const b = parseInt(color.substr(4, 2), 16);
                        const brightness = (r * 299 + g * 587 + b * 114) / 1000;
                        textColor = brightness > 128 ? 'black' : 'white';
                    } catch (e) {
                        console.error("Error parsing color:", backgroundColor, e);
                        textColor = 'black';
                    }
                }
                countryCard.style.color = textColor;
                
                countryCard.textContent = country.name;
                
                if (!country.color) {
                    const indicator = document.createElement('span');
                    indicator.textContent = ' (no color)';
                    indicator.style.fontSize = '0.8em';
                    indicator.style.color = '#666';
                    countryCard.appendChild(indicator);
                }
                
                countriesGrid.appendChild(countryCard);
            });
            
            continentSection.appendChild(countriesGrid);
            continentsContainer.appendChild(continentSection);
        });
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
