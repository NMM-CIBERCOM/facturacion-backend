/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cibercom.facturacion_back.repository;

import com.cibercom.facturacion_back.model.FacturaMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaMongoRepository extends MongoRepository<FacturaMongo, String> {
}
