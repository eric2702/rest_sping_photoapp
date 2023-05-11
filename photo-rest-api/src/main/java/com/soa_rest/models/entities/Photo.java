package com.soa_rest.models.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tbl_photo")
public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_name", length = 100, nullable = false)
    private String name;

    @Column(name = "photo_description", length = 500, nullable = false)
    private String description;

    // date
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "photo_date")
    private Date date = new Date();

    public Date getDate() {
        return date;
    }

    private Integer userId;

    // @Lob
    // @Column(name = "imagedata", length = 1000)
    // private byte[] imageData;

    public Photo(Long id, String name, String description, Integer userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.userId = userId;

    }

    public Photo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

}
