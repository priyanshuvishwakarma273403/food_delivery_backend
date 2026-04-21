package com.delivery.foodDelivery.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "restaurants")
public class RestaurantDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, name = "name", analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, name = "cuisineType")
    private String cuisineType;

    @Field(type = FieldType.Keyword, name = "city")
    private String city;

    @Field(type = FieldType.Double, name = "rating")
    private Double rating;
}
