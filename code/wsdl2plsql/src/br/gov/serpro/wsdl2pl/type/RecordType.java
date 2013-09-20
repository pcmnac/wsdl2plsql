package br.gov.serpro.wsdl2pl.type;

import groovy.xml.QName;

import java.util.ArrayList;
import java.util.List;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.type.def.XsdTypeDef;
import br.gov.serpro.wsdl2pl.util.K;
import br.gov.serpro.wsdl2pl.util.SB;
import br.gov.serpro.wsdl2pl.util.U;

import com.predic8.schema.ComplexType;

public class RecordType extends Type
{
    private List<Field> members = new ArrayList<Field>();

    private ComplexType complexType;

    public RecordType(Context context, ComplexType complexType)
    {
        super(context);
        this.complexType = complexType;
    }

    public ComplexType getComplexType()
    {
        return complexType;
    }

    public List<Field> getMembers()
    {
        return members;
    }

    @Override
    public String getId()
    {
        String namespace = complexType.getSchema() != null ? complexType.getSchema().getTargetNamespace() : "";
        return new QName(namespace, complexType.getName()).toString();
    }

    public Field getField(ElementInfo elementInfo)
    {
        Field result = null;

        for (Field field : getMembers())
        {
            if (field.getElement().equals(elementInfo))
            {
                result = field;
                break;
            }
        }

        return result;
    }

    @Override
    public String decl(int indent)
    {
        IKeywordEmitter ke = getContext().getKeywordEmitter();

        SB type = new SB();

        type.l(indent, "%s %s %s %s (", ke.type(), getName(), ke.is(), ke.record());

        if (getMembers().isEmpty())
        {
            Field field = new Field(getContext(), this, new ElementInfo("id"));
            XsdTypeDef typeDef = new XsdTypeDef(getContext(), new QName(K.Uri.XML_SCHEMA, "string"));
            field.setType(typeDef);
            getMembers().add(field);
        }

        for (int i = 0; i < getMembers().size(); i++)
        {
            Field field = getMembers().get(i);

            type.l(indent + 1, "-- " + field.comments());
            type.a(indent + 1, field.decl());

            if (i < getMembers().size() - 1)
            {
                type.a(",");
            }
            type.l("");
        }

        type.l(indent, ");");

        return type.toString();
    }

    @Override
    public String forwardDecl()
    {
        return String.format("%s %s;", getContext().getKeywordEmitter().type(), getName());
    }

    @Override
    public String comments()
    {
        return getComplexType().getQname().toString();
    }

    private String getName()
    {
        ComplexType complexType = getComplexType();

        return U.toPlIdentifier(getContext().getSymbolNameEmitter()
                .record(getContext().getPrefix(complexType.getQname().getNamespaceURI()),
                        complexType.getQname().getLocalPart()));
    }
}
