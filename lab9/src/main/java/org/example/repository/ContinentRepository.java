
package org.example.repository;

import jakarta.persistence.EntityManager;
import org.example.model.Continent;

import java.util.List;
import java.util.Optional;

public class ContinentRepository extends AbstractRepository<Continent, Integer> {

    public ContinentRepository(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<Continent> findAll() {
        return findAllOrderedBy("name");
    }

    public Optional<Continent> findByName(String name) {
        return findSingleByJpql(
                "SELECT c FROM Continent c WHERE c.name = :name",
                "name", name
        );
    }

    public List<Continent> findByNameContaining(String namePart) {
        return findByJpql(
                "SELECT c FROM Continent c WHERE LOWER(c.name) LIKE LOWER(:namePart) ORDER BY c.name",
                "namePart", "%" + namePart + "%"
        );
    }

    public List<Continent> findWithCountries() {
        return findByJpql(
                "SELECT DISTINCT c FROM Continent c JOIN FETCH c.countries ORDER BY c.name"
        );
    }
}