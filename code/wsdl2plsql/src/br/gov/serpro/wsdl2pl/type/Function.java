package br.gov.serpro.wsdl2pl.type;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.parser.Context;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.util.SB;
import br.gov.serpro.wsdl2pl.util.U;

public class Function extends DerivedSymbol implements Identifiable<String>
{
    private List<Parameter> parameters = new ArrayList<Parameter>();
    private Parameter inputHeader;
    private Parameter outputHeader;

    private ITypeDef returnType;
    private ElementInfo returnElement;
    private String defaultLocation;
    private String soapAction;

    public Function(Context context, ElementInfo element)
    {
        super(context, element);
        // super(U.toPlIdentifier(K.FUNCTION_PREFFIX + name), "package", element);
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

    public String comments()
    {
        return new QName(getElement().getNamespace(), getElement().getName(), getContext().getPrefix(getElement().getNamespace())).toString();
    }

    public String decl(int indent)
    {

        SB signature = new SB();
        IKeywordEmitter ke = getContext().getKeywordEmitter();

        // FUNCTION functionName
        signature.a(indent, "%s %s", ke.function(), name());
        if (!getParameters().isEmpty())
        {
            signature.l(" (");
            for (int i = 0; i < getParameters().size(); i++)
            {
                Parameter param = getParameters().get(i);
                signature.l(indent + 1, "-- " + param.comments());
                signature.a(indent + 1, param.decl());

                signature.l(",");
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
            signature.l(indent + 1, "%s %s %s", getUrlParam().decl(), ke.defaultKey(), "'" + getDefaultLocation() + "'");

            signature.l(indent, ")");
        }
        signature.a(indent, String.format("%s %s", ke.returnKey(), getReturnType().emit()));

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
        return U.toPlIdentifier(getContext().getSymbolNameEmitter().function(getContext().getPrefix(getElement().getNamespace()),
                getElement().getName()));
    }

    @Override
    public String getId()
    {
        return getElement().getName();
    }
}
