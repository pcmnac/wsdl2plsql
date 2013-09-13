package br.gov.serpro.wsdl2pl.parser;

import groovy.xml.QName;

import java.util.ArrayList;
import java.util.List;

import br.gov.serpro.wsdl2pl.exception.ParsingException;
import br.gov.serpro.wsdl2pl.type.ElementInfo;
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
import com.predic8.wsdl.AbstractBinding;
import com.predic8.wsdl.AbstractSOAPBinding;
import com.predic8.wsdl.AbstractSOAPBody;
import com.predic8.wsdl.AbstractSOAPHeader;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingElement;
import com.predic8.wsdl.BindingInput;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.BindingOutput;
import com.predic8.wsdl.ExtensibilityOperation;
import com.predic8.wsdl.Input;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Output;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.Types;

public class OperationsParser
{
    private Context context;

    public OperationsParser(Context context)
    {
        this.context = context;
    }

    public void parse()
    {

        for (Service service : context.getDefs().getServices())
        {
            for (Port port : service.getPorts())
            {
                Binding binding = port.getBinding();

                Object protocol = binding.getProtocol();
                // System.out.println(binding);
                AbstractBinding abstractBinding = binding.getBinding();

                if (protocol.equals(K.Protocol.SOAP_1_1) || protocol.equals(K.Protocol.SOAP_1_2))
                {
                    AbstractSOAPBinding soapBinding = (AbstractSOAPBinding) abstractBinding;

                    String defaultStyle = (String) soapBinding.getStyle();
                    PortType portType = binding.getPortType();

                    for (Operation operation : portType.getOperations())
                    {
                        BindingOperation bindingOperation = binding.getOperation(operation.getName());

                        ExtensibilityOperation extensibilityOperation = bindingOperation.getOperation();

                        String operationStyle = extensibilityOperation.getStyle() != null ? extensibilityOperation.getStyle() : defaultStyle;

                        if (operationStyle.equals(K.Style.RPC))
                        {

                            BindingInput bindingInput = bindingOperation.getInput();
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

                            // Function
                            ElementInfo elementInfo = new ElementInfo(operation.getName(), inputSoapBody.getNamespace());
                            Function function = new Function(context, elementInfo);

                            // Input header

                            // TODO: remover esse código para usar o resultado do inputSoapHeader
                            Part inputHeaderPart = null;// operation.getOutput().getMessage().getPart("params");

                            if (inputSoapHeader != null || inputHeaderPart != null)
                            {
                                // Part inputHeaderPart = input.getMessage().getPart(inputSoapHeader.getPart());

                                Parameter inputHeaderParam = null;

                                if (inputHeaderPart.getType() != null)
                                {
                                    inputHeaderParam = new Parameter(context, function, new ElementInfo(inputHeaderPart.getName()), true);
                                    inputHeaderParam.setType(getTypeDef(inputHeaderPart.getType()));
                                }
                                else if (inputHeaderPart.getElement() != null)
                                {
                                    Element inputHeaderElement = context.findElement(inputHeaderPart.getElement());
                                    inputHeaderParam = new Parameter(context, function, new ElementInfo(inputHeaderElement), true);
                                    inputHeaderParam.setType(getTypeDef(inputHeaderElement.getType()));
                                }

                                if (inputHeaderParam != null)
                                {
                                    function.setInputHeader(inputHeaderParam);
                                }
                                else
                                {
                                    throw new RuntimeException("Output header part doesn't have type neither element attributes");
                                }

                            }

                            // ----------------

                            function.getParameters().addAll(extractParameters(inputSoapBody.getParts(), function));

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
                                Part outputHeaderPart = outputSoapHeader.getPart();

                                Parameter outputHeaderParam = null;

                                if (outputHeaderPart.getType() != null)
                                {
                                    outputHeaderParam = new Parameter(context, function, new ElementInfo(outputHeaderPart.getName()),
                                            Parameter.Direction.OUT, true);
                                    outputHeaderParam.setType(getTypeDef(outputHeaderPart.getType()));

                                }
                                else if (outputHeaderPart.getElement() != null)
                                {
                                    Element outputHeaderElement = context.findElement(outputHeaderPart.getElement());
                                    outputHeaderParam = new Parameter(context, function, new ElementInfo(outputHeaderElement),
                                            Parameter.Direction.OUT, true);
                                    outputHeaderParam.setType(getTypeDef(outputHeaderElement.getType()));
                                }

                                if (outputHeaderParam != null)
                                {
                                    function.setOutputHeader(outputHeaderParam);
                                }
                                else
                                {
                                    throw new RuntimeException("Output header part doesn't have type neither element attributes");
                                }

                            }

                            if (outputSoapBody.getParts().size() == 1)
                            {
                                Part part = outputSoapBody.getParts().get(0);
                                function.setReturnType(extractReturnType(part));
                                function.setReturnElement(new ElementInfo(part.getName()));
                            }
                            else
                            {
                                throw new RuntimeException("Output Body message must have one part!");
                            }

                            function.setDefaultLocation(port.getAddress().getLocation());
                            function.setSoapAction(extensibilityOperation.getSoapAction());

                            context.registerFunction(function);

                        }
                        else if (operationStyle.equals(K.Style.DOCUMENT))
                        {
                            Input input = operation.getInput();
                            Message inputMessage = input.getMessage();

                            if (inputMessage.getParts().size() == 1)
                            {
                                Part operationPart = inputMessage.getParts().get(0);

                                if (operationPart.getElement() != null)
                                {

                                    Element element = context.findElement(operationPart.getElement());

                                    ElementInfo elementInfo = new ElementInfo(element.getName(), element.getSchema().getTargetNamespace());

                                    Function function = new Function(context, elementInfo);

                                    ComplexType inputComplexType = getComplexType(element);

                                    function.getParameters().addAll(extractParameters(inputComplexType, function));

                                    Output output = operation.getOutput();
                                    Message outputMessage = output.getMessage();

                                    if (outputMessage.getParts().size() != 1)
                                    {
                                        throw new RuntimeException("Output message must have one part!");
                                    }

                                    Element outputElement = context.findElement(outputMessage.getParts().get(0).getElement());
                                    ComplexType resultComplexType = getComplexType(outputElement);

                                    Element resultElement = resultComplexType.getSequence().getElements().get(0);

                                    function.setReturnType(getTypeDef(resultElement.getType()));
                                    function.setReturnElement(new ElementInfo(resultElement.getName(), resultElement.getSchema().getTargetNamespace()));

                                    function.setDefaultLocation(port.getAddress().getLocation());
                                    function.setSoapAction(extensibilityOperation.getSoapAction());

                                    context.registerFunction(function);
                                }
                                else
                                {
                                    throw new RuntimeException("document style message part must define \"element\" attribute!");
                                }

                            }
                            else
                            {
                                throw new RuntimeException("Body must have only one <part> element!");
                            }
                        }
                    }

                    // for (BindingOperation operation : binding.getOperations()) {
                    // ExtensibilityOperation extensibilityOperation = operation
                    // .getOperation();
                    // System.out.println(" - " + operation.getName());
                    // System.out.println(extensibilityOperation.getStyle());
                    // }
                }
                else
                {
                    System.err.println("Protocol not supported: " + protocol);
                }
            }
        }
    }

    private List<Parameter> extractParameters(ComplexType message, Function function)
    {
        List<Parameter> parameters = new ArrayList<Parameter>();

        for (Element element : message.getSequence().getElements())
        {
            element = context.findElement(element);

            if (element.getType() != null)
            {
                ElementInfo elementInfo = new ElementInfo(element);
                Parameter parameter = new Parameter(context, function, elementInfo);

                ITypeDef parameterType = getTypeDef(element.getType());

                parameter.setType(parameterType);
                parameters.add(parameter);
            }
        }

        return parameters;
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
                    Parameter parameter = new Parameter(context, function, elementInfo);

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

    private ITypeDef getTypeDef(QName type)
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

    private ITypeDef getTypeDef(TypeDefinition type)
    {
        return getTypeDef(type.getQname());
    }

    private ComplexType getComplexType(Element element)
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

    private ITypeDef extractReturnType(Part part)
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

            if (complexType.getSequence().getElements().size() != 1)
            {
                throw new ParsingException("Unknown format: Return complex type should be only one child");
            }

            returnType = getTypeDef(complexType.getSequence().getElements().get(0).getType());

        }

        return returnType;
    }

}
