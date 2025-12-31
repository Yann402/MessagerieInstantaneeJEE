package com.messagerie.dao;

import java.util.List;

public interface GenericDAO<T> {
    T findById(int id);
    List<T> findAll();
    boolean save(T entity);
    boolean update(T entity);
    boolean delete(int id);
}