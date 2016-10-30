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
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
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

    private List<String> usedCustomTypes = new ArrayList<String>();

    private List<String> functionsToGenerate = new ArrayList<String>();

    private List<String> usedExceptions = new ArrayList<String>();

    private IKeywordEmitter keywordEmitter;

    private ISymbolNameEmitter symbolNameEmitter;

    private Definitions defs;

    private String protocol;

    private String soapFaultExceptionId;

    private String inputValidationExceptionId;

    private Map<String, String> namespacePrefixes = new HashMap<String, String>();

    private int exceptionId = 20000;

    private String[] services;

	private boolean debugging;

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

    private Map<String, Type> getComplexTypeMap()
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

    public Type getCustomType(String typeId)
    {
        return getComplexTypeMap().get(typeId);
    }

    public Collection<Type> getCustomTypes()
    {
        return getComplexTypeMap().values();
    }

    public void registerCustomType(Type type)
    {
        getComplexTypeMap().put(type.getId(), type);
    }

    public void registerFunction(Function function)
    {
        getPlFunctions().add(function);
    }

    public void registerException(Exception exception, String id)
    {
        if (!exceptions.containsKey(exception.getId()))
        {
            exceptions.put(id, exception);
        }
    }

    public void registerException(Exception exception)
    {
        registerException(exception, exception.getId());
    }

    public Collection<Exception> getExceptions()
    {
        return exceptions.values();
    }

    public void registerUsedType(ITypeDef type)
    {
        if (!usedCustomTypes.contains(type.getId()))
        {
            usedCustomTypes.add(type.getId());
        }
    }

    public boolean isElegible(Type type)
    {
        return usedCustomTypes.contains(type.getId());
    }

    public void registerUsedException(Exception exception)
    {
        if (!usedExceptions.contains(exception.getId()))
        {
            usedExceptions.add(exception.getId());

            if (exception.getType() != null)
            {
                registerUsedType(exception.getType());
            }
        }
    }

    public boolean isElegible(Exception exception)
    {
        return usedExceptions.contains(exception.getId());
    }

    public boolean isElegible(Function function)
    {
        return functionsToGenerate.contains(function.getElement().getName());
    }

    public void makeElegible(String functionName)
    {
        functionsToGenerate.add(functionName);
    }

    public void clear()
    {
        functionsToGenerate.clear();
        usedCustomTypes.clear();
        usedExceptions.clear();
        U.clear();
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

        int count = 1;
        for (String prefix : nsc.keySet())
        {
            if ("".equals(prefix))
            {
                prefix = "ns" + count++;

                while (namespacePrefixes.containsValue(prefix))
                {
                    prefix = "ns" + count++;
                }
                namespacePrefixes.put(nsc.get(""), prefix);
            }
            else
            {
                namespacePrefixes.put(nsc.get(prefix), prefix);
            }
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
            String prefix = namespacePrefixes.get(uri);
            decl += "xmlns:" + prefix + "=\"" + uri + "\" ";
        }

        return decl;
    }

    public void registerSoapFaultException(Exception exception)
    {
        registerException(exception);
        soapFaultExceptionId = exception.getId();
    }

    public Exception getSoapFaultException()
    {
        return exceptions.get(soapFaultExceptionId);
    }

    public void registerInputValidationException(Exception exception)
    {
        registerException(exception);
        inputValidationExceptionId = exception.getId();
    }

    public Exception getInputValidationException()
    {
        return exceptions.get(inputValidationExceptionId);
    }

    public void resolveProtocol(String preferredProtocol)
    {
        String[] supported = { K.Protocol.SOAP_1_2, K.Protocol.SOAP_1_1 };

        if (getDefs().getServices().size() > 0)
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
                            return;
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

    public int nextExceptionId()
    {
        return -++exceptionId;
    }

    public String[] getServices()
    {
        return services;
    }

    public void setServices(String[] services)
    {
        this.services = services;
    }

	public void setDebuggingMode(boolean b) {
		this.debugging = b;
	}

	public boolean isDebuggingMode() {
		return this.debugging;
	}

}
