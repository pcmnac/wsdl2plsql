package br.gov.serpro.wsdl2pl.parser;

import java.util.Arrays;

import br.gov.serpro.wsdl2pl.Context;

import com.predic8.wsdl.AbstractBinding;
import com.predic8.wsdl.AbstractSOAPBinding;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.ExtensibilityOperation;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;

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
            if (context.getServices() == null || Arrays.asList(context.getServices()).contains(service.getName()))
            {
                for (Port port : service.getPorts())
                {
                    Binding binding = port.getBinding();

                    Object protocol = binding.getProtocol();

                    AbstractBinding abstractBinding = binding.getBinding();

                    if (protocol.equals(context.getProtocol()))
                    {
                        AbstractSOAPBinding soapBinding = (AbstractSOAPBinding) abstractBinding;

                        String defaultStyle = (String) soapBinding.getStyle();
                        PortType portType = binding.getPortType();

                        for (Operation operation : portType.getOperations())
                        {
                            BindingOperation bindingOperation = binding.getOperation(operation.getName());

                            ExtensibilityOperation extensibilityOperation = bindingOperation.getOperation();

                            String operationStyle = extensibilityOperation.getStyle() != null ? extensibilityOperation
                                    .getStyle() : defaultStyle;

                            OperationParser parser = OperationParser.getInstance(context, operationStyle);

                            parser.parse(port, operation, bindingOperation);
                        }
                    }
                }
            }
        }
    }
}
