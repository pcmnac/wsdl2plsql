package br.gov.serpro.wsdl2pl.type;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.util.SB;
import br.gov.serpro.wsdl2pl.util.U;

public class Function extends DerivedSymbol implements Identifiable<String>
{
    private List<Parameter> parameters = new ArrayList<Parameter>();
    private Parameter inputHeader;
    private Parameter outputHeader;
    private String documentation;

    private ITypeDef returnType;
    private ElementInfo returnElement;
    private String defaultLocation;
    private String soapAction;
    private List<Exception> exceptions = new ArrayList<Exception>();

    public Function(Context context, ElementInfo element)
    {
        super(context, element);
    }

    public List<Parameter> getParameters()
    {
        return parameters;
    }

    public ITypeDef getReturnType()
    {
        return returnType;
    }

    public void setReturnType(ITypeDef returnType)
    {
        this.returnType = returnType;
    }

    public ElementInfo getReturnElement()
    {
        return returnElement;
    }

    public void setReturnElement(ElementInfo returnElement)
    {
        this.returnElement = returnElement;
    }

    public String getDefaultLocation()
    {
        return defaultLocation;
    }

    public void setDefaultLocation(String defaultLocation)
    {
        this.defaultLocation = defaultLocation;
    }

    public String getSoapAction()
    {
        return soapAction;
    }

    public void setSoapAction(String soapAction)
    {
        this.soapAction = soapAction;
    }

    public Parameter getInputHeader()
    {
        return inputHeader;
    }

    public void setInputHeader(Parameter inputHeader)
    {
        this.inputHeader = inputHeader;
    }

    public Parameter getOutputHeader()
    {
        return outputHeader;
    }

    public void setOutputHeader(Parameter outputHeader)
    {
        this.outputHeader = outputHeader;
    }

    public String getDocumentation()
    {
        return documentation;
    }

    public void setDocumentation(String documentation)
    {
        this.documentation = documentation;
    }

    public List<Exception> getExceptions()
    {
        return exceptions;
    }

    public void addException(Exception exception)
    {
        getExceptions().add(exception);
    }

    public String comments(int indent)
    {
        String comments = new QName(getElement().getNamespace(), getElement().getName(), getContext().getPrefix(
                getElement().getNamespace())).toString();

        if (documentation != null)
        {
            comments += "\n" + documentation;
        }

        return formatDocumentation(indent, comments);
    }

    public String doc(int indent)
    {
        SB function = new SB();

        function.l(indent, "/**");
        function.a(comments(indent));
        function.l(indent, " * ");
        function.l(indent, " * @author wsdl2plsql (generated)");
        function.l(indent, " * ");

        for (Parameter parameter : getParameters())
        {
            function.l(indent, " * @param %s %s", parameter.name(), parameter.getElement());
        }
        if (getInputHeader() != null)
        {
            function.l(indent, " * @param %s %s", getInputHeader().name(), getInputHeader().getElement());
        }
        if (getOutputHeader() != null)
        {
            function.l(indent, " * @param %s %s", getOutputHeader().name(), getOutputHeader().getElement());
        }
        function.l(indent, " * @param %s %s", getUrlParam().name(), "Service URL");

        if (!isVoid())
        {
            function.l(indent, " * ");
            function.l(indent, " * @return %s", getReturnElement());
        }

        function.l(indent, " * ");

        function.l(indent, " * @throws " + getContext().getSoapFaultException().name());

        for (Exception exception : getExceptions())
        {
            function.l(indent, " * @throws " + exception.name());
        }

        function.l(indent, " * ");
        function.l(indent, " */ ");

        return function.toString();
    }

    public static String formatDocumentation(int indent, String text)
    {
        SB doc = new SB();
        String lines[] = text.split("\\n");

        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];

            if ((i > 0 && i < lines.length - 1) || !line.trim().isEmpty())
            {
                doc.l(indent, " * " + line);
            }
        }

        return doc.toString();
    }

    public boolean isVoid()
    {
        return getReturnType() == null;
    }

    public String decl(int indent, boolean header)
    {

        SB signature = new SB();
        IKeywordEmitter ke = getContext().getKeywordEmitter();

        // FUNCTION functionName
        signature.a(indent, "%s %s", (!isVoid() ? ke.function() : ke.procedure()), name());

        signature.l(" (");

        if (!getParameters().isEmpty())
        {
            for (int i = 0; i < getParameters().size(); i++)
            {
                Parameter param = getParameters().get(i);
                signature.l(indent + 1, "-- " + param.comments());
                signature.a(indent + 1, param.decl());

                signature.l(",");
            }
        }
        if (getInputHeader() != null)
        {
            signature.l(indent + 1, "-- " + getInputHeader().comments());
            signature.l(indent + 1, getInputHeader().decl() + ",");
        }

        if (getOutputHeader() != null)
        {
            signature.l(indent + 1, "-- " + getOutputHeader().comments());
            signature.l(indent + 1, getOutputHeader().decl() + ",");
        }

        signature.l(indent + 1, "-- url");
        if (header)
        {
            signature
                    .l(indent + 1, "%s %s %s", getUrlParam().decl(), ke.defaultKey(), "'" + getDefaultLocation() + "'");
        }
        else
        {
            signature.l(indent + 1, getUrlParam().decl());
        }

        signature.a(indent, ")");

        if (!isVoid())
        {
            signature.l();
            signature.a(indent,
                    String.format("%s %s", ke.returnKey(), getReturnType().emit().replaceAll("\\([\\W\\w]+\\)", "")));
        }

        return signature.toString();
    }

    public PreDefinedParameter getUrlParam()
    {
        String baseName = "url";
        String name = baseName;
        int count = 1;
        boolean repeated = false;
        PreDefinedParameter url = null;

        do
        {
            repeated = false;
            url = new PreDefinedParameter(getContext(), name, getContext().getKeywordEmitter().varchar2(), getId());

            for (Parameter parameter : getParameters())
            {
                if (parameter.name().equals(url.name()))
                {
                    name = baseName + count++;
                    repeated = true;
                    break;
                }
            }
        }
        while (repeated);

        return url;
    }

    public String name()
    {
        String name = null;

        if (isVoid())
        {
            name = U.toPlIdentifier(getContext().getSymbolNameEmitter().procedure(
                    getContext().getPrefix(getElement().getNamespace()), getElement().getName()));
        }
        else
        {
            name = U.toPlIdentifier(getContext().getSymbolNameEmitter().function(
                    getContext().getPrefix(getElement().getNamespace()), getElement().getName()));
        }

        return name;
    }

    @Override
    public String getId()
    {
        return getElement().getName();
    }
}
