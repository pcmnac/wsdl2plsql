package br.gov.serpro.wsdl2pl;

import groovy.xml.QName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.emitter.ISymbolNameEmitter;
import br.gov.serpro.wsdl2pl.exception.ParsingException;
import br.gov.serpro.wsdl2pl.type.Exception;
import br.gov.serpro.wsdl2pl.type.Function;
import br.gov.serpro.wsdl2pl.type.Type;
import br.gov.serpro.wsdl2pl.util.K;
import br.gov.serpro.wsdl2pl.util.U;

import com.predic8.schema.Element;
import com.predic8.schema.SimpleType;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.Service;

/**
 * @author 04343650413
 * 
 */
public class Context
{

    private String packageName;

    private List<Type> plTypes = new ArrayList<Type>();

    private List<Function> plFunctions = new ArrayList<Function>();

    private Map<String, Type> complexTypeMap = new HashMap<String, Type>();

    private Map<QName, SimpleType> simpleTypeMap = new HashMap<QName, SimpleType>();

    private Map<String, Exception> exceptions = new HashMap<String, Exception>();

    private IKeywordEmitter keywordEmitter;

    private ISymbolNameEmitter symbolNameEmitter;

    private Definitions defs;

    private String protocol;

    private Exception soapFaultException;

    private Map<String, String> namespacePrefixes = new HashMap<String, String>();

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public Context(Definitions defs)
    {
        this.defs = defs;
        mapNamespaces();
    }

    public List<Type> getPlTypes()
    {
        return plTypes;
    }

    public void setPlTypes(List<Type> plTypes)
    {
        this.plTypes = plTypes;
    }

    public List<Function> getPlFunctions()
    {
        return plFunctions;
    }

    public Map<String, Type> getComplexTypeMap()
    {
        return complexTypeMap;
    }

    public Map<QName, SimpleType> getSimpleTypeMap()
    {
        return simpleTypeMap;
    }

    public Definitions getDefs()
    {
        return defs;
    }

    public IKeywordEmitter getKeywordEmitter()
    {
        return keywordEmitter;
    }

    public void setKeywordEmitter(IKeywordEmitter keywordEmitter)
    {
        this.keywordEmitter = keywordEmitter;
    }

    public ISymbolNameEmitter getSymbolNameEmitter()
    {
        return symbolNameEmitter;
    }

    public void setSymbolNameEmitter(ISymbolNameEmitter symbolNameEmitter)
    {
        this.symbolNameEmitter = symbolNameEmitter;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public boolean containsSimpleType(QName simpleTypeName)
    {
        return getSimpleTypeMap().containsKey(simpleTypeName);
    }

    public SimpleType getSimpleType(QName simpleTypeName)
    {
        return getSimpleTypeMap().get(simpleTypeName);
    }

    public boolean containsCustomType(String typeId)
    {
        return getComplexTypeMap().containsKey(typeId);
    }

    public void registerCustomType(Type type)
    {
        getComplexTypeMap().put(type.getId(), type);
    }

    public void registerFunction(Function function)
    {
        getPlFunctions().add(function);
    }

    public void registerException(Exception exception)
    {
        if (!exceptions.containsKey(exception.getId()))
        {
            exceptions.put(exception.getId(), exception);
        }
    }

    public Collection<Exception> getExceptions()
    {
        return exceptions.values();
    }

    public Element findElement(Element element)
    {
        return U.findElement(element, getDefs());
    }

    public String getPrefix(String namespace)
    {
        return namespacePrefixes.containsKey(namespace) ? namespacePrefixes.get(namespace) : "";
    }

    @SuppressWarnings("unchecked")
    private void mapNamespaces()
    {
        Map<String, String> nsc = (Map<String, String>) getDefs().getNamespaceContext();

        for (String prefix : nsc.keySet())
        {
            namespacePrefixes.put(nsc.get(prefix), prefix);
        }

        if (!namespacePrefixes.containsKey(K.Uri.SOAP_ENVELOPE))
        {
            String prefix = "soapenv";
            int i = 0;

            while (namespacePrefixes.containsValue(prefix))
            {
                prefix = "soapenv" + i++;
            }

            namespacePrefixes.put(K.Uri.SOAP_ENVELOPE, prefix);
        }
    }

    public String toQName(String name, String namespace)
    {
        if (namespacePrefixes.containsKey(namespace))
        {
            name = namespacePrefixes.get(namespace) + ":" + name;
        }

        return name;
    }

    public String generateNamespaceDeclarations()
    {

        String decl = "";

        for (String uri : namespacePrefixes.keySet())
        {
            decl += "xmlns:" + namespacePrefixes.get(uri) + "=\"" + uri + "\" ";
        }

        return decl;
    }

    public void registerSoapFaultException(Exception exception)
    {
        registerException(exception);
        soapFaultException = exception;
    }

    public Exception getSoapFaultException()
    {
        return soapFaultException;
    }

    public void setSoapFaultException(Exception soapFaultException)
    {
        this.soapFaultException = soapFaultException;
    }

    public void resolveProtocol(String preferredProtocol)
    {
        String[] supported = { K.Protocol.SOAP_1_2, K.Protocol.SOAP_1_1 };

        if (getDefs().getServices().size() == 1)
        {
            Service service = getDefs().getServices().get(0);
            for (String supportedProtocol : supported)
            {
                for (Port port : service.getPorts())
                {
                    Binding binding = port.getBinding();

                    String protocol = (String) binding.getProtocol();

                    if (protocol.equals(supportedProtocol))
                    {
                        setProtocol(protocol);
                        if (preferredProtocol == null || protocol.equals(preferredProtocol))
                        {
                            break;
                        }
                    }
                }
            }

            if (getProtocol() == null)
            {
                throw new ParsingException("No supported protocol found in WSDL.");
            }
        }
        else
        {
            throw new ParsingException("More than one service is not supported.");
        }
    }
}
