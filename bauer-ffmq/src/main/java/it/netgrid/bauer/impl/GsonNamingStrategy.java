package it.netgrid.bauer.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.google.gson.FieldNamingStrategy;

public class GsonNamingStrategy implements FieldNamingStrategy {

    @Override
    public String translateName(Field paramField) {

        Annotation annotationName = null;

        if(null != (annotationName = paramField.getAnnotation(XmlElement.class))){
            return ((XmlElement) annotationName).name();
        }else if(null != (annotationName = paramField.getAnnotation(XmlAttribute.class))){
            return ((XmlAttribute)annotationName).name();
        }
        return paramField.getName();
    }
    
}
