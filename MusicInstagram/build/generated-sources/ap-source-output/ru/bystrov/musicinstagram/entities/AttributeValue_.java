package ru.bystrov.musicinstagram.entities;

import java.sql.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import ru.bystrov.musicinstagram.entities.Objects;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2016-04-04T20:44:03")
@StaticMetamodel(AttributeValue.class)
public class AttributeValue_ { 

    public static volatile SingularAttribute<AttributeValue, Date> dateValue;
    public static volatile SingularAttribute<AttributeValue, String> stringValue;
    public static volatile SingularAttribute<AttributeValue, Objects> ref;
    public static volatile SingularAttribute<AttributeValue, Long> attrId;
    public static volatile SingularAttribute<AttributeValue, Long> attrValueId;
    public static volatile SingularAttribute<AttributeValue, Long> objId;
    public static volatile SingularAttribute<AttributeValue, Long> numberValue;

}