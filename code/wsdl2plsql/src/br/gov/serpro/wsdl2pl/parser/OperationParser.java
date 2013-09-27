package br.gov.serpro.wsdl2pl.parser;

import groovy.xml.QName;
import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.exception.ParsingException;
import br.gov.serpro.wsdl2pl.type.ElementInfo;
import br.gov.serpro.wsdl2pl.type.Exception;
import br.gov.serpro.wsdl2pl.type.Function;
import br.gov.serpro.wsdl2pl.type.Parameter;
import br.gov.serpro.wsdl2pl.type.def.ComplexTypeDef;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.type.def.SimpleTypeDef;
import br.gov.serpro.wsdl2pl.type.def.XsdTypeDef;
import br.gov.serpro.wsdl2pl.util.K;
import br.gov.serpro.wsdl2pl.util.U;

import com.predic8.schema.ComplexType;
import com.predic8.schema.Element;
import com.predic8.schema.Schema;
import com.predic8.schema.TypeDefinition;
import com.predic8.wsdl.AbstractSOAPBody;
import com.predic8.wsdl.AbstractSOAPFault;
import com.predic8.wsdl.AbstractSOAPHeader;
import com.predic8.wsdl.BindingElement;
import com.predic8.wsdl.BindingFault;
import com.predic8.wsdl.BindingInput;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.BindingOutput;
import com.predic8.wsdl.ExtensibilityOperation;
import com.predic8.wsdl.Fault;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.Types;

public abstract class OperationParser
{
    private Context context;

    protected OperationParser()
    {
    }

    public static OperationParser getInstance(Context context, String style)
    {
        OperationParser parser = null;

        if (style.equals(K.Style.RPC))
        {
            parser = RpcStyleOperationParser.getInstance();
        }
        else if (style.equals(K.Style.DOCUMENT))
        {
            parser = DocumentStyleOperationParser.getInstance();
        }

        if (parser != null)
        {
            parser.setContext(context);
        }

        return parser;
    }

    public Context getContext()
    {
        return context;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public void parse(Port port, Operation operation, BindingOperation bindingOperation)
    {
        BindingInput bindingInput = bindingOperation.getInput();
        ExtensibilityOperation extensibilityOperation = bindingOperation.getOperation();

        AbstractSOAPBody inputSoapBody = null;
        AbstractSOAPHeader inputSoapHeader = null;

        for (BindingElement bindingElement : bindingInput.getBindingElements())
        {
            if (bindingElement instanceof AbstractSOAPBody)
            {
                inputSoapBody = (AbstractSOAPBody) bindingElement;
            }
            else if (bindingElement instanceof AbstractSOAPHeader)
            {
                inputSoapHeader = (AbstractSOAPHeader) bindingElement;
            }
        }

        // FUNCTION

        Function function = createFunctionAndInputParameters(operation, inputSoapBody);

        // INPUT HEADER

        if (inputSoapHeader != null)
        {
            Parameter inHeaderParameter = partToParam(inputSoapHeader.getPart(), function, Parameter.Direction.IN, true);
            function.setInputHeader(inHeaderParameter);
        }

        // OUTPUT

        AbstractSOAPBody outputSoapBody = null;
        AbstractSOAPHeader outputSoapHeader = null;

        BindingOutput bindingOutput = bindingOperation.getOutput();

        for (BindingElement bindingElement : bindingOutput.getBindingElements())
        {
            if (bindingElement instanceof AbstractSOAPBody)
            {
                outputSoapBody = (AbstractSOAPBody) bindingElement;
            }
            else if (bindingElement instanceof AbstractSOAPHeader)
            {
                outputSoapHeader = (AbstractSOAPHeader) bindingElement;
            }
        }

        if (outputSoapHeader != null)
        {
            Parameter outHeaderParameter = partToParam(outputSoapHeader.getPart(), function, Parameter.Direction.OUT,
                    true);
            function.setOutputHeader(outHeaderParameter);
        }

        if (outputSoapBody != null)
        {
            if (outputSoapBody.getParts() == null || outputSoapBody.getParts().isEmpty())
            {
                System.out.println("Function output have no parts:" + function.getElement().getName());
            }
            else if (outputSoapBody.getParts().size() == 1)
            {
                Part part = outputSoapBody.getParts().get(0);
                function.setReturnType(extractReturnType(part));
                if (function.getReturnType() != null)
                {
                    function.setReturnElement(getResultElement(part));
                }
            }
            else
            {
                throw new RuntimeException("Output Body message must have at most one part!");
            }
        }
        else
        {
            System.out.println("Function without output:" + function.getElement().getName());
        }

        // FAULTS

        processFaults(operation, bindingOperation, function);

        function.setDefaultLocation(port.getAddress().getLocation());
        function.setSoapAction(extensibilityOperation.getSoapAction());

        getContext().registerFunction(function);
    }

    protected abstract Function createFunctionAndInputParameters(Operation operation, AbstractSOAPBody inputSoapBody);

    protected abstract ElementInfo getResultElement(Part part);

    protected ITypeDef getTypeDef(QName type)
    {
        ITypeDef def = null;

        if (U.isNativeSchemaType(type))
        {
            def = new XsdTypeDef(context, type);
        }
        else if (context.containsSimpleType(type))
        {
            def = new SimpleTypeDef(context, context.getSimpleType(type));
        }
        else
        {
            def = new ComplexTypeDef(context, type);
        }

        return def;
    }

    protected ITypeDef getTypeDef(TypeDefinition type)
    {
        return getTypeDef(type.getQname());
    }

    protected ComplexType getComplexType(Element element)
    {
        ComplexType complexType = null;

        if (element.getEmbeddedType() != null)
        {
            if (element.getEmbeddedType() instanceof ComplexType)
            {
                complexType = (ComplexType) element.getEmbeddedType();
            }
        }
        else if (element.getType() != null)
        {
            for (Types types : context.getDefs().getTypes())
            {
                for (Schema schema : types.getAllSchemas())
                {
                    TypeDefinition typeDefinition = schema.getType(element.getType());
                    if (typeDefinition != null && typeDefinition instanceof ComplexType)
                    {
                        complexType = (ComplexType) typeDefinition;
                        break;
                    }
                }
            }
        }

        return complexType;
    }

    protected ITypeDef extractReturnType(Part part)
    {
        ITypeDef returnType = null;
        if (part.getType() != null)
        {
            returnType = getTypeDef(part.getType());
        }
        else if (part.getElement() != null)
        {
            Element resultElement = context.findElement(part.getElement());
            ComplexType complexType = getComplexType(resultElement);

            if (complexType.getSequence().getElements().size() > 1)
            {
                throw new ParsingException("Return complex type should be at most one child:" + complexType);
            }

            if (!complexType.getSequence().getElements().isEmpty())
            {
                returnType = getTypeDef(complexType.getSequence().getElements().get(0).getType());
            }
        }

        return returnType;
    }

    protected Parameter partToParam(Part part, Function function, Parameter.Direction direction, boolean header)
    {
        Parameter param = null;
        if (part.getType() != null)
        {
            param = new Parameter(getContext(), function, new ElementInfo(part.getName()), direction, header);
            param.setType(getTypeDef(part.getType()));

        }
        else if (part.getElement() != null)
        {
            Element element = getContext().findElement(part.getElement());
            param = new Parameter(getContext(), function, new ElementInfo(element), direction, header);
            param.setType(getTypeDef(element.getType()));
        }

        if (param == null)
        {
            throw new RuntimeException("Part doesn't have neither type nor element attributes");
        }

        return param;

    }

    protected void processFaults(Operation operation, BindingOperation bindingOperation, Function function)
    {

        for (BindingFault bindingFault : bindingOperation.getFaults())
        {
            AbstractSOAPFault abstractSOAPFault = null;
            for (BindingElement bindingElement : bindingFault.getBindingElements())
            {
                if (bindingElement instanceof AbstractSOAPFault)
                {
                    abstractSOAPFault = (AbstractSOAPFault) bindingElement;
                }
            }

            if (abstractSOAPFault != null)
            {
                for (Fault fault : operation.getFaults())
                {
                    if (fault.getQName().equals(abstractSOAPFault.getQName()))
                    {
                        if (fault.getMessage().getParts().size() == 1)
                        {
                            Part part = fault.getMessage().getParts().get(0);
                            Exception exception = null;

                            if (part.getType() != null)
                            {
                                exception = new Exception(getContext(), new ElementInfo(part.getName()));
                                exception.setType(getTypeDef(part.getType()));
                            }
                            else if (part.getElement() != null)
                            {
                                Element partElement = getContext().findElement(part.getElement());
                                exception = new Exception(getContext(), new ElementInfo(partElement));
                                exception.setType(getTypeDef(partElement.getType()));
                            }

                            if (exception != null)
                            {
                                function.addException(exception);
                                getContext().registerException(exception);
                            }
                        }
                        else
                        {
                            throw new ParsingException("Fault message MUST have a single part.");
                        }
                        break;
                    }
                }

            }
        }

    }

}
