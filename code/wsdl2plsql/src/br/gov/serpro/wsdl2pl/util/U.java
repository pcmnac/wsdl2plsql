package br.gov.serpro.wsdl2pl.util;

import groovy.xml.QName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;

import com.predic8.schema.Element;
import com.predic8.wsdl.Definitions;

public class U
{
    private static class NameScope
    {
        private List<String> names = new ArrayList<String>();
        private Map<String, Integer> partIndexMap = new HashMap<String, Integer>();
        private Map<String, String> cache = new HashMap<String, String>();

        public List<String> getNames()
        {
            return names;
        }

        public Map<String, Integer> getPartIndexMap()
        {
            return partIndexMap;
        }

        public Map<String, String> getCache()
        {
            return cache;
        }
    }

    public static void clear()
    {
        nameScopes.clear();
    }

    private static Map<String, NameScope> nameScopes = new HashMap<String, NameScope>();

    public static boolean isNativeSchemaType(QName type)
    {
        return type.getNamespaceURI().equals(K.Uri.XML_SCHEMA);
    }

    public static String toFieldName(String name, String record)
    {
        return truncIdentifier(toUnderscored(name.replaceAll("-", "_")), record, null);
    }

    public static String toPlIdentifier(String name)
    {
        return toPlIdentifier(name, null);
    }

    public static String toPlIdentifier(String name, String suffix)
    {
        return toPlIdentifier(name, suffix, "package");
    }

    public static String toPlIdentifier(String name, String suffix, String scope)
    {
        return truncIdentifier(name, scope, suffix);
    }

    public static Element findElement(String name, Definitions definitions)
    {
        return findElement(definitions.getElement((name)), definitions);
    }

    public static Element findElement(Element element, Definitions definitions)
    {
        while (element != null && element.getRef() != null)
        {
            element = definitions.getElement((element.getRef()));
        }

        return element;
    }

    public static String baseTypeToPlType(QName name, Context context)
    {
        String plType = null;

        String[] stringTypes = { "string", "ENTITIES", "ENTITY", "ID", "IDREF", "IDREFS", "language", "Name", "NCName",
                "NMTOKEN", "NMTOKENS", "normalizedString", "QName", "token" };

        String[] longStringTypes = { "base64Binary" };

        String[] dateTypes = { "date", "dateTime", "time" };

        String[] numberTypes = { "decimal", "double", "float" };

        String[] integerTypes = { "byte", "int", "integer", "long", "negativeInteger", "nonNegativeInteger",
                "nonPositiveInteger", "positiveInteger", "short", "unsignedLong", "unsignedInt", "unsignedShort",
                "unsignedByte" };

        String xsdType = name.getLocalPart();

        IKeywordEmitter ke = context.getKeywordEmitter();

        if (Arrays.asList(stringTypes).contains(xsdType))
        {
            plType = ke.varchar2() + "(32767)";
        }
        else if (Arrays.asList(longStringTypes).contains(xsdType))
        {
            plType = ke.clob();
        }
        else if (Arrays.asList(dateTypes).contains(xsdType))
        {
            plType = ke.date();
        }
        else if (Arrays.asList(numberTypes).contains(xsdType))
        {
            plType = ke.number();
        }
        else if (Arrays.asList(integerTypes).contains(xsdType))
        {
            plType = ke.integer();
        }
        else if ("boolean".equals(xsdType))
        {
            plType = ke.booleanKey();
        }
        else
        {
            throw new RuntimeException("Unsupported type: " + xsdType);
        }

        return plType;
    }

    private static final Map<String, String> TO_MASKS = new HashMap<String, String>();
    private static final Map<String, String> FROM_MASKS = new HashMap<String, String>();

    static
    {
        TO_MASKS.put("boolean", "CASE $var WHEN true THEN 'true' ELSE 'false' END");
        TO_MASKS.put("date", "TO_CHAR($var, 'YYYY-MM-DD')");
        TO_MASKS.put("time", "TO_CHAR($var, 'HH24:MI:SS')");
        TO_MASKS.put("datetime", "REPLACE(TO_CHAR($var,'YYYY-MM-DD HH24:MI:SS'),' ','T')");
        TO_MASKS.put("decimal", "TRIM(TO_CHAR($var, '9999999999.99'))");
        TO_MASKS.put("double", "TRIM(TO_CHAR($var, '9999999999.99'))");
        TO_MASKS.put("float", "TRIM(TO_CHAR($var, '9999999999.99'))");

        FROM_MASKS.put("decimal", "TO_NUMBER($var, '9999999999.99')");
        FROM_MASKS.put("boolean", "CASE LOWER($var) WHEN 'true' THEN true ELSE false END");
        FROM_MASKS.put("date", "TO_DATE($var, 'YYYY-MM-DD')");
        FROM_MASKS.put("time", "TO_DATE($var, 'HH24:MI:SS')");
        FROM_MASKS.put("datetime", "TO_DATE(REPLACE($var,'T',' '), 'YYYY-MM-DD HH24:MI:SS')");
        FROM_MASKS.put("integer", "TO_NUMBER($var)");
        FROM_MASKS.put("int", "TO_NUMBER($var)");
        FROM_MASKS.put("byte", "TO_NUMBER($var)");
        FROM_MASKS.put("short", "TO_NUMBER($var)");
        FROM_MASKS.put("long", "TO_NUMBER($var)");
    }

    public static String stringToBaseType(String xsdType, String var)
    {
        String result = var;

        if (FROM_MASKS.containsKey(xsdType.toLowerCase()))
        {
            result = "(" + FROM_MASKS.get(xsdType.toLowerCase()).replaceAll("\\$var", var) + ")";
        }

        return result;

    }

    public static String baseTypeToString(String xsdType, String var)
    {
        String result = var;

        if (TO_MASKS.containsKey(xsdType.toLowerCase()))
        {
            result = "(" + TO_MASKS.get(xsdType.toLowerCase()).replaceAll("\\$var", var) + ")";
        }

        return result;

    }

    public static QName qNameToQName(javax.xml.namespace.QName qName)
    {
        return new QName(qName.getNamespaceURI(), qName.getLocalPart(), qName.getPrefix());
    }

    public static javax.xml.namespace.QName qNameToQName(QName qName)
    {
        return new javax.xml.namespace.QName(qName.getNamespaceURI(), qName.getLocalPart(), qName.getPrefix());
    }

    public static String truncIdentifier(String name, String scope, String suffix)
    {
        if (suffix != null && !name.endsWith(suffix))
        {
            name += suffix;
        }

        String result = name;
        int limit = 30;

        NameScope used = null;
        if (nameScopes.containsKey(scope))
        {
            used = nameScopes.get(scope);
        }
        else
        {
            used = new NameScope();
            nameScopes.put(scope, used);
        }

        if (used.getCache().containsKey(name + suffix))
        {
            result = used.getCache().get(name + suffix);
        }
        else
        {

            if (name.length() > limit)
            {
                result = trunc(name, suffix, limit, used);
            }

            if (!used.getNames().contains(result))
            {
                used.getNames().add(result);
            }
            else
            {
                result = trunc(name, suffix, limit, used);
            }

            used.getCache().put(name + suffix, result);

        }

        return result;
    }

    private static String trunc(String name, String suffix, int limit, NameScope used)
    {
        int indexChars = 2;
        String part = name.substring(0, limit - (indexChars + 1 + (suffix != null ? suffix.length() : 0)));
        int index = 0;

        String result = name;

        int increment = 1;
        do
        {
            if (used.getPartIndexMap().containsKey(part))
            {
                index = used.getPartIndexMap().get(part) + increment++;

                if (index < Math.pow(10, indexChars))
                {
                    used.getPartIndexMap().put(part, index);
                }
                else
                {
                    throw new RuntimeException("Maximum repeated part name reached");
                }
            }
            else
            {
                used.getPartIndexMap().put(part, index);
            }

            result = part + "$" + String.format("%02d", index) + (suffix != null ? suffix : "");
        }
        while (used.getNames().contains(result));

        return result;
    }

    public static String toUnderscored(String camelCaseString)
    {
        return camelCaseString.replaceAll("-", "_").replaceAll("([a-z])([A-Z])", "$1_$2");
    }

    public static void main(String[] args)
    {
        String[] names = { "get_abc_pro$00_t", "getAbcPropertyCC", "getXyzPropertyCC", "getPropertyLong1",
                "getPropertyLong2", "getPropertyLong3", "getPropertyLong4", "getPropertyLong5", "getPropertyLong6",
                "getPropertyLong7", "getPropertyLong8", "getPropertyLong9", "getPropertyLong10", "getPropertyLong11",
                "generateHashCode", "get_abc_pro$01_t" };

        for (String property : names)
        {
            System.out.println(property + "\t>\t" + truncIdentifier(toUnderscored(property), "teste", "_t") + " : "
                    + truncIdentifier(toUnderscored(property), "teste", "_t").length());
        }
    }
}
