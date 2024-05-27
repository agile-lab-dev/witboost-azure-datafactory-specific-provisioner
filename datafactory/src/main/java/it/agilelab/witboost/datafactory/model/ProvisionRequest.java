package it.agilelab.witboost.datafactory.model;

public record ProvisionRequest<T>(DataProduct dataProduct, Component<T> component, Boolean removeData) {}
