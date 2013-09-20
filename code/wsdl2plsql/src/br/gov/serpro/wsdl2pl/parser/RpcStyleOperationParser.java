package br.gov.serpro.wsdl2pl.parser;

import java.util.ArrayList;
import java.util.List;

import br.gov.serpro.wsdl2pl.type.ElementInfo;
import br.gov.serpro.wsdl2pl.type.Function;
import br.gov.serpro.wsdl2pl.type.Parameter;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;

import com.predic8.wsdl.AbstractSOAPBody;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;

public class RpcStyleOperationParser extends OperationParser
{
    private static final RpcStyleOperationParser INSTANCE = new RpcStyleOperationParser();

    private RpcStyleOperationParser()
    {
    }

    public static RpcStyleOperationParser getInstance()
    {
        return INSTANCE;
    }

    private List<Parameter> extractParameters(List<Part> parts, Function function)
    {
        List<Parameter> parameters = new ArrayList<Parameter>();

        if (parts.size() > 0)
        {
            for (Part part : parts)
            {
                if (part.getType() != null)
                {
                    ElementInfo elementInfo = new ElementInfo(part.getName());
                    Parameter parameter = new Parameter(getContext(), function, elementInfo);

                    ITypeDef parameterType = getTypeDef(part.getType());

                    parameter.setType(parameterType);
                    parameters.add(parameter);
                }
            }
        }
        else
        {
            throw new RuntimeException("No parts found in message");
        }

        return parameters;
    }

    @Override
    protected Function createFunctionAndInputParameters(Operation operation, AbstractSOAPBody inputSoapBody)
    {
        ElementInfo elementInfo = new ElementInfo(operation.getName(), inputSoapBody.getNamespace());
        Function function = new Function(getContext(), elementInfo);
        function.getParameters().addAll(extractParameters(inputSoapBody.getParts(), function));
        return function;
    }
}
