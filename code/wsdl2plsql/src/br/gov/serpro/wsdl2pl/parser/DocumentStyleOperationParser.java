package br.gov.serpro.wsdl2pl.parser;

import java.util.ArrayList;
import java.util.List;

import br.gov.serpro.wsdl2pl.exception.ParsingException;
import br.gov.serpro.wsdl2pl.type.ElementInfo;
import br.gov.serpro.wsdl2pl.type.Function;
import br.gov.serpro.wsdl2pl.type.Parameter;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;

import com.predic8.schema.ComplexType;
import com.predic8.schema.Element;
import com.predic8.wsdl.AbstractSOAPBody;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;

public class DocumentStyleOperationParser extends OperationParser
{
    private static final DocumentStyleOperationParser INSTANCE = new DocumentStyleOperationParser();

    private DocumentStyleOperationParser()
    {
    }

    public static DocumentStyleOperationParser getInstance()
    {
        return INSTANCE;
    }

    private List<Parameter> extractParameters(ComplexType message, Function function)
    {
        List<Parameter> parameters = new ArrayList<Parameter>();

        if (message.getSequence() != null && message.getSequence().getElements() != null)
        {
            for (Element element : message.getSequence().getElements())
            {
                element = getContext().findElement(element);

                if (element.getType() != null)
                {
                    ElementInfo elementInfo = new ElementInfo(element);
                    Parameter parameter = new Parameter(getContext(), function, elementInfo);

                    ITypeDef parameterType = getTypeDef(element.getType());

                    parameter.setType(parameterType);
                    parameters.add(parameter);
                }
            }
        }

        return parameters;
    }

    @Override
    protected Function createFunctionAndInputParameters(Operation operation, AbstractSOAPBody inputSoapBody)
    {
        Function function = null;

        if (inputSoapBody.getParts().size() == 1)
        {
            Part operationPart = inputSoapBody.getParts().get(0);

            if (operationPart.getElement() != null)
            {
                // Function

                Element operationElement = getContext().findElement(operationPart.getElement());
                ElementInfo operationElementInfo = new ElementInfo(operationElement);
                function = new Function(getContext(), operationElementInfo);

                ComplexType inputComplexType = getComplexType(operationElement);

                function.getParameters().addAll(extractParameters(inputComplexType, function));
            }
            else
            {
                throw new RuntimeException("document style message part must define \"element\" attribute!");
            }

        }
        else
        {
            throw new RuntimeException("Body must have a single <part> element!");
        }

        return function;
    }

    @Override
    protected ElementInfo getResultElement(Part part)
    {
        ElementInfo elementInfo = null;

        if (part.getElement() != null)
        {
            Element resultElement = getContext().findElement(part.getElement());
            ComplexType complexType = getComplexType(resultElement);

            if (complexType.getSequence().getElements().size() != 1)
            {
                throw new ParsingException("Unknown format: Return complex type should be only one child");
            }

            elementInfo = new ElementInfo(complexType.getSequence().getElements().get(0));
        }
        else
        {
            throw new ParsingException(
                    "In document/wrapped style, result part element must be a complex type with a single element");
        }

        return elementInfo;
    }
}
