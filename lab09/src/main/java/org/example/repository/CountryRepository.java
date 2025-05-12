package org.example.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.model.Country;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CountryRepository {

    private final EntityManager entityManager;

    public CountryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Country> findAll() {
        TypedQuery<Country> query = entityManager.createQuery(
                "SELECT c FROM Country c ORDER BY c.name", Country.class);
        return query.getResultList();
    }

    public Optional<Country> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Country.class, id));
    }

    public List<Country> findByContinent(Integer continentId) {
        TypedQuery<Country> query = entityManager.createQuery(
                "SELECT c FROM Country c WHERE c.continent.id = :continentId ORDER BY c.name", Country.class);
        query.setParameter("continentId", continentId);
        return query.getResultList();
    }

    public Optional<Country> findByCode(String code) {
        TypedQuery<Country> query = entityManager.createQuery(
                "SELECT c FROM Country c WHERE c.code = :code", Country.class);
        query.setParameter("code", code);
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Country> findByName(String name) {
        TypedQuery<Country> query = entityManager.createQuery(
                "SELECT c FROM Country c WHERE c.name = :name", Country.class);
        query.setParameter("name", name);
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Country> findByNameContaining(String namePart) {
        TypedQuery<Country> query = entityManager.createQuery(
                "SELECT c FROM Country c WHERE LOWER(c.name) LIKE LOWER(:namePart) ORDER BY c.name", Country.class);
        query.setParameter("namePart", "%" + namePart + "%");
        return query.getResultList();
    }

    public List<Country> searchCountries(String searchTerm, Integer continentId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Country> cq = cb.createQuery(Country.class);
        Root<Country> country = cq.from(Country.class);

        List<Predicate> predicates = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String term = "%" + searchTerm.toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(country.get("name")), term);
            Predicate codePredicate = cb.like(cb.lower(country.get("code")), term);
            predicates.add(cb.or(namePredicate, codePredicate));
        }

        if (continentId != null) {
            predicates.add(cb.equal(country.get("continent").get("id"), continentId));
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        cq.orderBy(cb.asc(country.get("name")));
        TypedQuery<Country> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    public List<Country> findCountriesWithCapitals() {
        TypedQuery<Country> query = entityManager.createQuery(
                "SELECT DISTINCT c FROM Country c JOIN c.cities city WHERE city.isCapital = true ORDER BY c.name", Country.class);
        return query.getResultList();
    }

    public Country save(Country country) {
        if (country.getId() == null) {
            entityManager.persist(country);
            return country;
        } else {
            return entityManager.merge(country);
        }
    }

    public void delete(Country country) {
        entityManager.remove(entityManager.contains(country) ? country : entityManager.merge(country));
    }

    public void deleteById(Integer id) {
        findById(id).ifPresent(this::delete);
    }

    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(c) FROM Country c", Long.class);
        return query.getSingleResult();
    }

    public long countByContinent(Integer continentId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(c) FROM Country c WHERE c.continent.id = :continentId", Long.class);
        query.setParameter("continentId", continentId);
        return query.getSingleResult();
    }

    public void bulkSave(List<Country> countries) {
        if (countries == null || countries.isEmpty()) {
            return;
        }


        boolean isActiveTransaction = entityManager.getTransaction().isActive();
        if (!isActiveTransaction) {
            entityManager.getTransaction().begin();
        }

        try {
            int count = 0;
            for (Country country : countries) {
                if (country.getId() == null) {
                    entityManager.persist(country);
                } else {
                    entityManager.merge(country);
                }

                // Flush and clear the persistence context periodically to avoid memory issues
                if (++count % 50 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }

            if (!isActiveTransaction) {
                entityManager.getTransaction().commit();
            }
        } catch (Exception e) {
            if (!isActiveTransaction) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Error while bulk saving countries", e);
        }
    }
}