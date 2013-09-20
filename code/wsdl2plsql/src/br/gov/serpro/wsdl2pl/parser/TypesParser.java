package br.gov.serpro.wsdl2pl.parser;

import groovy.xml.QName;
import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.exception.ParsingException;
import br.gov.serpro.wsdl2pl.type.ElementInfo;
import br.gov.serpro.wsdl2pl.type.Exception;
import br.gov.serpro.wsdl2pl.type.Field;
import br.gov.serpro.wsdl2pl.type.RecordType;
import br.gov.serpro.wsdl2pl.type.VarrayType;
import br.gov.serpro.wsdl2pl.type.def.ArrayTypeDef;
import br.gov.serpro.wsdl2pl.type.def.ComplexTypeDef;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.type.def.SimpleTypeDef;
import br.gov.serpro.wsdl2pl.type.def.XsdTypeDef;
import br.gov.serpro.wsdl2pl.util.K;
import br.gov.serpro.wsdl2pl.util.U;

import com.predic8.schema.ComplexType;
import com.predic8.schema.Element;
import com.predic8.schema.ModelGroup;
import com.predic8.schema.Schema;
import com.predic8.schema.SchemaComponent;
import com.predic8.schema.Sequence;
import com.predic8.schema.SimpleType;
import com.predic8.wsdl.Types;

public class TypesParser
{
    private Context context;

    public TypesParser(Context context)
    {
        this.context = context;
    }

    public void parse()
    {
        for (Types types : context.getDefs().getTypes())
        {

            for (Schema schema : types.getSchemas())
            {
                for (SimpleType simpleType : schema.getSimpleTypes())
                {
                    dissectSimpleType(simpleType);
                }

                addSoapFaultType();

                for (ComplexType complexType : schema.getComplexTypes())
                {
                    dissectComplexType(complexType);
                }
            }
        }
    }

    public void addSoapFaultType()
    {
        ComplexType complexType = null;

        if (context.getProtocol().equals(K.Protocol.SOAP_1_1))
        {
            complexType = buildSoap11FaultComplexType();
        }
        else if (context.getProtocol().equals(K.Protocol.SOAP_1_2))
        {
            complexType = buildSoap12FaultComplexType();
        }

        dissectComplexType(complexType);

        Exception exception = new Exception(context, new ElementInfo("SoapFault"));
        exception.setType(new ComplexTypeDef(context, complexType.getQname()));

        context.registerSoapFaultException(exception);
    }

    private ComplexType buildSoap11FaultComplexType()
    {
        ComplexType fault = new ComplexType();
        fault.setName("Fault");
        fault.setQname(new QName(K.Uri.SOAP_ENVELOPE, "Fault"));
        fault.setModel(new Sequence());

        Schema schema = new Schema();
        schema.setTargetNamespace(K.Uri.SOAP_ENVELOPE);
        schema.add(fault);

        Element faultCode = new Element();
        faultCode.setName("faultcode");
        faultCode.setType(new QName(K.Uri.XML_SCHEMA, "string"));
        fault.getSequence().add(faultCode);

        Element faultString = new Element();
        faultString.setName("faultstring");
        faultString.setType(new QName(K.Uri.XML_SCHEMA, "string"));
        fault.getSequence().add(faultString);

        Element faultActor = new Element();
        faultActor.setName("faultactor");
        faultActor.setType(new QName(K.Uri.XML_SCHEMA, "string"));
        fault.getSequence().add(faultActor);

        return fault;
    }

    private ComplexType buildSoap12FaultComplexType()
    {
        Schema schema = new Schema();
        schema.setTargetNamespace(K.Uri.SOAP_ENVELOPE);
        schema.setElementFormDefault("qualified");

        // SubCode
        ComplexType subCode = new ComplexType();
        subCode.setName("SubCode");
        subCode.setQname(new QName(K.Uri.SOAP_ENVELOPE, subCode.getName()));
        subCode.setModel(new Sequence());
        schema.add(subCode);

        Element valueElement = new Element();
        valueElement.setName("Value");
        valueElement.setType(new QName(K.Uri.XML_SCHEMA, "string"));
        valueElement.setSchema(schema);
        subCode.getSequence().add(valueElement);

        dissectComplexType(subCode);

        // Code
        ComplexType code = new ComplexType();
        code.setName("Code");
        code.setQname(new QName(K.Uri.SOAP_ENVELOPE, code.getName()));
        code.setModel(new Sequence());
        schema.add(code);

        code.getSequence().add(valueElement);

        Element subCodeElement = new Element();
        subCodeElement.setName(subCode.getName());
        subCodeElement.setType(subCode.getQname());
        subCodeElement.setSchema(schema);
        code.getSequence().add(subCodeElement);

        dissectComplexType(code);

        // Reason

        ComplexType reason = new ComplexType();
        reason.setName("Reason");
        reason.setQname(new QName(K.Uri.SOAP_ENVELOPE, reason.getName()));
        reason.setModel(new Sequence());
        schema.add(reason);

        Element textElement = new Element();
        textElement.setName("Text");
        textElement.setType(new QName(K.Uri.XML_SCHEMA, "string"));
        textElement.setSchema(schema);
        reason.getSequence().add(textElement);

        dissectComplexType(reason);

        // Fault

        ComplexType fault = new ComplexType();
        fault.setName("Fault");
        fault.setQname(new QName(K.Uri.SOAP_ENVELOPE, "Fault"));
        fault.setModel(new Sequence());

        schema.add(fault);

        Element codeElement = new Element();
        codeElement.setName(code.getName());
        codeElement.setType(code.getQname());
        codeElement.setSchema(schema);
        fault.getSequence().add(codeElement);

        Element reasonElement = new Element();
        reasonElement.setName(reason.getName());
        reasonElement.setType(reason.getQname());
        reasonElement.setSchema(schema);
        fault.getSequence().add(reasonElement);

        Element nodeElement = new Element();
        nodeElement.setName("Node");
        nodeElement.setType(new QName(K.Uri.XML_SCHEMA, "string"));
        nodeElement.setSchema(schema);
        fault.getSequence().add(nodeElement);

        Element roleElement = new Element();
        roleElement.setName("Role");
        roleElement.setType(new QName(K.Uri.XML_SCHEMA, "string"));
        roleElement.setSchema(schema);
        fault.getSequence().add(roleElement);

        dissectComplexType(fault);

        return fault;
    }

    private void dissectComplexType(ComplexType complexType)
    {
        // System.out.println("Complex Type: " + complexType.getName());
        RecordType recordType = new RecordType(context, complexType);
        SchemaComponent schemaComponent = complexType.getModel();

        // Choice, All ou Sequence
        if (schemaComponent instanceof ModelGroup)
        {
            ModelGroup modelGroup = (ModelGroup) schemaComponent;

            for (Element element : modelGroup.getElements())
            {
                boolean isArray = element.getMaxOccurs().equals("unbounded")
                        || Integer.parseInt(element.getMaxOccurs()) > 1;

                element = context.findElement(element);

                Field field = new Field(context, recordType, new ElementInfo(element));

                ITypeDef fieldType = null;

                if (element.getType() != null)
                {
                    if (U.isNativeSchemaType(element.getType()))
                    {
                        fieldType = new XsdTypeDef(context, element.getType());
                    }
                    else if (context.containsSimpleType(element.getType()))
                    {
                        fieldType = new SimpleTypeDef(context, context.getSimpleType(element.getType()));
                    }
                    else
                    {
                        fieldType = new ComplexTypeDef(context, element.getType());
                    }
                }
                else
                {
                    throw new ParsingException(String.format(
                            "Element <%s> must define its type. Complex type sub-elements must define their type.",
                            element.getName()));
                }

                if (isArray)
                {
                    VarrayType arrayType = new VarrayType(context, fieldType);

                    fieldType = new ArrayTypeDef(context, arrayType.getType());

                    if (!context.containsCustomType(arrayType.getId()))
                    {
                        // context.getPlTypes().add(arrayType);
                        context.registerCustomType(arrayType);
                    }
                }

                fieldType.setRequired(Integer.parseInt(element.getMinOccurs()) > 0);

                field.setType(fieldType);

                recordType.getMembers().add(field);
            }
        }

        // context.getPlTypes().add(recordType);
        context.registerCustomType(recordType);
    }

    private void dissectSimpleType(SimpleType simpleType)
    {
        // System.out.println("Simple Type: " + simpleType.getName());
        // System.out.println(simpleType.getBuildInTypeName());
        // System.out.println(simpleType.getName());
        // System.out.println(simpleType.getQname());
        context.getSimpleTypeMap().put(simpleType.getQname(), simpleType);

        // BaseRestriction baseRestriction = simpleType.getRestriction();
        //
        // System.out.println("base: " + baseRestriction.getBase().getLocalPart());
        //
        // System.out.println(baseRestriction.getClass());
        // if (baseRestriction instanceof StringRestriction)
        // {
        // StringRestriction stringRestriction = (StringRestriction) baseRestriction;
        //
        // for (Facet facet : stringRestriction.getFacets())
        // {
        // System.out.println(facet);
        // }
        //
        // for (EnumerationFacet enumerationFacet : stringRestriction.getEnumerationFacets())
        // {
        // System.out.println(enumerationFacet.getValue());
        // }
        // }
        // else
        // {
        //
        // }
        //
        // for (Facet facet : baseRestriction.getFacets())
        // {
        // System.out.println(facet);
        // }
        //
        // for (EnumerationFacet enumerationFacet : baseRestriction.getEnumerationFacets())
        // {
        // System.out.println(enumerationFacet.getValue());
        // }

    }

    // private void sortTypes()
    // {
    // List<Type> sortedTypes = new ArrayList<Type>();
    //
    // for (Type plType : context.getPlTypes())
    // {
    // addType(plType, context.getComplexTypeMap(), sortedTypes);
    // }
    //
    // if (sortedTypes.size() != context.getPlTypes().size())
    // {
    // throw new RuntimeException();
    // }
    //
    // context.setPlTypes(sortedTypes);
    //
    // }

    // private void addType(Type plType, Map<String, Type> complexTypeMap, List<Type> target)
    // {
    // if (!plType.getDependencies().isEmpty())
    // {
    // for (String typeName : plType.getDependencies())
    // {
    // Type dependencyType = complexTypeMap.get(typeName);
    // if (!target.contains(dependencyType))
    // {
    // addType(dependencyType, complexTypeMap, target);
    // }
    // }
    // }
    //
    // if (!target.contains(plType))
    // {
    // target.add(plType);
    // }
    // }

}
