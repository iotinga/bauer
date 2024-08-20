package it.netgrid.bauer.impl;

public record StreamEvent<P>(String topic, P payload) {}
