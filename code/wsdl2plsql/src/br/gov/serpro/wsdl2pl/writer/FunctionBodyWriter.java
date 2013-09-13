package br.gov.serpro.wsdl2pl.writer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.parser.Context;
import br.gov.serpro.wsdl2pl.type.ElementInfo;
import br.gov.serpro.wsdl2pl.type.Field;
import br.gov.serpro.wsdl2pl.type.Function;
import br.gov.serpro.wsdl2pl.type.LocalVar;
import br.gov.serpro.wsdl2pl.type.Parameter;
import br.gov.serpro.wsdl2pl.type.RecordType;
import br.gov.serpro.wsdl2pl.type.VarrayType;
import br.gov.serpro.wsdl2pl.type.def.ArrayTypeDef;
import br.gov.serpro.wsdl2pl.type.def.ComplexTypeDef;
import br.gov.serpro.wsdl2pl.type.def.ITypeDef;
import br.gov.serpro.wsdl2pl.util.K;
import br.gov.serpro.wsdl2pl.util.SB;
import br.gov.serpro.wsdl2pl.util.U;

public class FunctionBodyWriter extends BaseWriter
{
    private Context context;

    private static final int BODY_INDENT = 1;

    public FunctionBodyWriter(Context context)
    {
        this.context = context;
    }

    public String writeFunctionsBody() throws IOException
    {
        SB functions = new SB();
        IKeywordEmitter ke = context.getKeywordEmitter();

        // TODO: tratar
        String packageName = context.getPackageName();

        // CREATE OR REPLACE PACKAGE BODY packageName AS
        functions.l("%s %s %s %s %s\n", ke.createOrReplace(), ke.packageKey(), ke.body(), packageName, ke.as());

        functions.a(generatePostFunction());
        functions.a("\n");

        if (!context.getPlFunctions().isEmpty())
        {
            for (Function function : context.getPlFunctions())
            {
                functions.l(BODY_INDENT, "-- " + function.comments());
                functions.a(writeFunctionBody(function) + "\n");
            }
        }

        // END packageName;
        functions.l("%s %s;", ke.end(), packageName);

        return functions.toString();
    }

    private String toQName(ElementInfo element)
    {
        return context.toQName(element.getName(), element.getNamespace());
    }

    private String writeFunctionBody(Function function)
    {
        IKeywordEmitter ke = context.getKeywordEmitter();
        SB body = new SB();

        LocalVar varResult = new LocalVar(context, "result", function.getReturnType().emit(), function.getId());
        LocalVar varRequest = new LocalVar(context, "request", ke.clob(), function.getId());
        LocalVar varResponseText = new LocalVar(context, "responseText", ke.clob(), function.getId());
        LocalVar varResponse = new LocalVar(context, "response", ke.xmlType(), function.getId());
        LocalVar varResponseHeader = new LocalVar(context, "responseHeader", ke.xmlType(), function.getId());
        LocalVar varResponseBody = new LocalVar(context, "responseBody", ke.xmlType(), function.getId());
        LocalVar varNsMap = new LocalVar(context, "nsMap", ke.clob(), function.getId());
        LocalVar varTempNode = new LocalVar(context, "tempNode", ke.xmlType(), function.getId());

        body.l("%s %s", function.decl(BODY_INDENT), ke.as());

        body.l(BODY_INDENT + 1, varResult.decl());
        body.l(BODY_INDENT + 1, varRequest.decl());
        body.l(BODY_INDENT + 1, varResponseText.decl());
        body.l(BODY_INDENT + 1, varResponse.decl());
        if (function.getOutputHeader() != null)
        {
            body.l(BODY_INDENT + 1, varResponseHeader.decl());
        }
        body.l(BODY_INDENT + 1, varResponseBody.decl());
        body.l(BODY_INDENT + 1, varTempNode.decl());
        body.l(BODY_INDENT + 1, varNsMap.decl("'" + context.generateNamespaceDeclarations() + "'"));

        body.l(BODY_INDENT, ke.begin());

        body.l(BODY_INDENT + 1, "%s := ", varRequest.name());
        body.l(BODY_INDENT + 2, "'<" + toQName(K.Elem.ENVELOPE) + " ' || " + varNsMap.name() + " || '>' ||");

        // Request header

        if (function.getInputHeader() != null)
        {
            body.l(BODY_INDENT + 3, "'<" + toQName(K.Elem.HEADER) + ">' ||");
            body.a(generateParameterRequestString(varRequest, "", function.getInputHeader().getType(), function.getInputHeader().name(), function
                    .getInputHeader().getElement(), BODY_INDENT + 4));
            body.l(BODY_INDENT + 3, "'</" + toQName(K.Elem.HEADER) + ">' ||");
        }
        else
        {
            body.l(BODY_INDENT + 3, "'<" + toQName(K.Elem.HEADER) + "/>' ||");
        }

        body.l(BODY_INDENT + 3, "'<" + toQName(K.Elem.BODY) + ">' ||");
        body.l(BODY_INDENT + 4, "'<" + toQName(function.getElement()) + ">' ||");

        if (!function.getParameters().isEmpty())
        {
            for (Parameter parameter : function.getParameters())
            {
                body.a(generateParameterRequestString(varRequest, "", parameter.getType(), parameter.name(), parameter.getElement(), BODY_INDENT + 5));

                // U.a(body,
                // generateParameterRequestString(varRequest, "", function.getReturnType(), varResult.emitName(),
                // function.getReturnElement(),
                // BODY_INDENT + 5));

            }
        }

        body.l(BODY_INDENT + 4, "'</" + toQName(function.getElement()) + ">' ||");
        body.l(BODY_INDENT + 3, "'</" + toQName(K.Elem.BODY) + ">' ||");
        body.l(BODY_INDENT + 2, "'</" + toQName(K.Elem.ENVELOPE) + ">';");
        body.l("\n");

        String soapAction = (function.getSoapAction() != null ? "'" + function.getSoapAction() + "'" : ke.nullKey());

        // responseText = post(url, request, soapAction);
        body.l(BODY_INDENT + 1, "%s := fc_post(%s, %s, %s);", varResponseText.name(), function.getUrlParam().name(), varRequest.name(), soapAction);

        // response = XmlType.createXml(responseText);
        body.l(BODY_INDENT + 1, "%s := %s.createXml(%s);\n", varResponse.name(), ke.xmlType(), varResponseText.name());

        // IF response.existsNode("/envelope/body/fault") THEN
        body.l(BODY_INDENT + 1, "%s %s.existsNode('/%s/%s/%s', %s) = 1 %s", ke.ifKey(), varResponse.name(), toQName(K.Elem.ENVELOPE),
                toQName(K.Elem.BODY), toQName(K.Elem.FAULT), varNsMap.name(), ke.then());
        // RAISE_APPLICATION_ERROR(-20003, soapFault)
        body.l(BODY_INDENT + 2, "%s(%s, %s.extract('/%s/%s/%s', %s).getStringVal());", "RAISE_APPLICATION_ERROR", "-20003", varResponse.name(),
                toQName(K.Elem.ENVELOPE), toQName(K.Elem.BODY), toQName(K.Elem.FAULT), varNsMap.name());
        // // ELSE
        // U.l(body, BODY_INDENT + 1, ke.elseKey());

        // END IF;
        body.l(BODY_INDENT + 1, "%s %s;\n", ke.end(), ke.ifKey());

        if (function.getOutputHeader() != null)
        {
            // responseHeader = response.extract("/envelope/header/*/")
            body.l(BODY_INDENT + 1, "%s := %s.extract('/%s/%s/child::node()', %s);", varResponseHeader.name(), varResponse.name(),
                    toQName(K.Elem.ENVELOPE), toQName(K.Elem.HEADER), varNsMap.name());

            body.l(generateResultExtractString(varResponseHeader, varNsMap, varTempNode, "", function.getOutputHeader().getType(), function
                    .getOutputHeader().name(), "", function.getOutputHeader().getElement(), null, BODY_INDENT + 1));
        }

        // responseBody = response.extract("/envelope/body/*/")
        body.l(BODY_INDENT + 1, "%s := %s.extract('/%s/%s/*/child::node()', %s);", varResponseBody.name(), varResponse.name(),
                toQName(K.Elem.ENVELOPE), toQName(K.Elem.BODY), varNsMap.name());

        body.l(generateResultExtractString(varResponseBody, varNsMap, varTempNode, "", function.getReturnType(), varResult.name(), "",
                function.getReturnElement(), null, BODY_INDENT + 1));

        body.l(BODY_INDENT + 1, "%s %s;", ke.returnKey(), varResult.name());
        body.l(BODY_INDENT, "%s %s;", ke.end(), function.name());

        return body.toString();
    }

    private String generateParameterRequestString(LocalVar request, String prefix, ITypeDef type, String name, ElementInfo element, int level)
    {
        SB body = new SB();
        IKeywordEmitter ke = context.getKeywordEmitter();

        if (type instanceof ComplexTypeDef)
        {
            RecordType recordType = (RecordType) context.getComplexTypeMap().get(type.getId());

            if (element.isOptional())
            {
                body.l("'';");

                // DECLARE
                body.l(level, ke.declare());

                LocalVar recordXml = new LocalVar(context, "recordXml" + level, ke.varchar2() + "(32767)", "");
                body.l(level + 1, recordXml.decl());

                // BEGIN
                body.l(level, ke.begin());

                body.l(level + 1, "%s := ", recordXml.name());

                for (Field field : recordType.getMembers())
                {
                    String fieldName = field.name();
                    body.a(generateParameterRequestString(recordXml, prefix + name + ".", field.getType(), fieldName, field.getElement(), level + 1));
                }

                body.l(level + 1, "'';");

                // IF temp IS NOT NULL THEN
                body.l(level + 1, "%s %s %s %s %s %s ", ke.ifKey(), recordXml.name(), ke.is(), ke.not(), ke.nullKey(), ke.then());

                body.l(level + 2, "%s := %s || '<%s>' || %s || '</%s>';", request.name(), request.name(), toQName(element), recordXml.name(),
                        toQName(element));

                // END IF;
                body.l(level + 1, "%s %s;", ke.end(), ke.ifKey());

                // END;
                body.l(level, "%s;", ke.end());

                // wl_request := wl_request ||
                body.l(level, "%s := %s ||\n", request.name(), request.name());
            }
            else
            {
                body.a(level, "'<" + toQName(element) + ">' || ");

                for (Field field : recordType.getMembers())
                {
                    String fieldName = field.name();
                    body.a(generateParameterRequestString(request, prefix + name + ".", field.getType(), fieldName, field.getElement(), level + 1));
                }

                body.l();
                body.l(level, "'</" + toQName(element) + ">' ||");
            }
        }
        else if (type instanceof ArrayTypeDef)
        {
            VarrayType varrayType = (VarrayType) context.getComplexTypeMap().get(type.getId());

            int baseLevel = level - BODY_INDENT;
            body.l(level, "'';\n");

            String loopVarName = "i" + level;

            // IF varray IS NOT NULL AND varray.count > 0 THEN
            body.l(baseLevel + 1, "%s %s %s %s %s %s %s.%s > 0 %s", ke.ifKey(), prefix + name, ke.is(), ke.not(), ke.nullKey(), ke.and(), prefix
                    + name, ke.count(), ke.then());

            // FOR item IN varray.FIRST..varray.LAST LOOP
            body.l(baseLevel + 1, "%s %s %s %s.%s..%s.%s %s", ke.forKey(), loopVarName, ke.in(), prefix + name, ke.first(), prefix + name, ke.last(),
                    ke.loop());

            body.l(level + 2, "%s := %s ||", request.name(), request.name());
            body.a(generateParameterRequestString(request, prefix, varrayType.getType(), name + "(" + loopVarName + ")", element, level + 2));
            body.l(level + 2, "'';");

            // END LOOP;
            body.l(baseLevel + 2, "%s %s;", ke.end(), ke.loop());

            // END LOOP;
            body.l(baseLevel + 1, "%s %s;\n", ke.end(), ke.ifKey());

            body.l(level, "%s := %s ||\n", request.name(), request.name());

        }
        else
        {
            if (element.isOptional())
            {
                body.l("'';\n");
                body.l(level, "%s %s %s %s %s %s", ke.ifKey(), prefix + name, ke.is(), ke.not(), ke.nullKey(), ke.then());

                body.a(level + 1, "%s := %s || ", request.name(), request.name());
                body.l("'<" + toQName(element) + ">' || " + U.baseTypeToString(type.getXsdType().getLocalPart(), prefix + name) + " || '</"
                        + toQName(element) + ">';");

                body.l(level, "%s %s;", ke.end(), ke.ifKey());

                body.a(level, "%s := %s || ", request.name(), request.name());

            }
            else
            {
                body.l();
                body.a(level, "'<" + toQName(element) + ">' || " + U.baseTypeToString(type.getXsdType().getLocalPart(), prefix + name) + " || '</"
                        + toQName(element) + ">' || ");
            }
        }

        return body.toString();
    }

    private String generateResultExtractString(LocalVar response, LocalVar varNsMap, LocalVar varTempNode, String prefix, ITypeDef type, String name,
            String pathPrefix, ElementInfo element, String loopVar, int level)
    {
        SB body = new SB();
        IKeywordEmitter ke = context.getKeywordEmitter();

        if (type instanceof ComplexTypeDef)
        {
            RecordType recordType = (RecordType) context.getComplexTypeMap().get(type.getId());
            for (Field field : recordType.getMembers())
            {
                String fieldName = field.name();
                body.a(generateResultExtractString(response, varNsMap, varTempNode, prefix + name + ".", field.getType(), fieldName, pathPrefix + "/"
                        + toXPathNode(element, loopVar), field.getElement(), null, level));
            }
        }
        else if (type instanceof ArrayTypeDef)
        {
            VarrayType varrayType = (VarrayType) context.getComplexTypeMap().get(type.getId());
            int baseLevel = level - BODY_INDENT;

            String loopVarName = "i" + level;

            body.l();
            // DECLARE
            body.l(baseLevel + 1, ke.declare());
            // i number := 1;
            body.l(baseLevel + 2, "%s %s := 1;", loopVarName, ke.number());
            // BEGIN
            body.l(baseLevel + 1, ke.begin());
            // WHILE response.existsNode('path[i]') = 1 LOOP
            body.l(baseLevel + 2, "%s %s.existsNode('%s/%s[' || %s || ']', %s) = 1 %s", ke.whileKey(), response.name(), pathPrefix,
                    toXPathNode(element, null), loopVarName, varNsMap.name(), ke.loop());

            // IF varray IS NULL THEN
            body.l();
            body.l(baseLevel + 3, "-- if %s was not initialized yet...", prefix + name);
            body.l(baseLevel + 3, "%s %s %s %s %s", ke.ifKey(), prefix + name, ke.is(), ke.nullKey(), ke.then());
            // varray = vaType();
            body.l(baseLevel + 4, "-- initializes %s", prefix + name);
            body.l(baseLevel + 4, "%s := %s();", prefix + name, type.emit());
            // END IF;
            body.l(baseLevel + 3, "%s %s;\n", ke.end(), ke.ifKey());

            body.l(baseLevel + 3, "-- extends varray %s", prefix + name);
            body.l(baseLevel + 3, "%s.extend(1);", prefix + name);

            body.a(generateResultExtractString(response, varNsMap, varTempNode, prefix, varrayType.getType(), name + "(" + loopVarName + ")",
                    pathPrefix, element, loopVarName, level + 2));
            body.l(baseLevel + 3, loopVarName + " := " + loopVarName + " + 1;");
            // END LOOP;
            body.l(baseLevel + 2, "%s %s;", ke.end(), ke.loop());
            // END;
            body.l(baseLevel + 1, "%s;", ke.end());

        }
        else
        {
            // Utils.l(body, level, "IF wl_response_body.existsNode('" + elementPrefix + "/" + toXPathNode(elementName,
            // loopVar) + "') = 1 THEN");

            // tempNode := response.extrac('path');
            body.l();
            body.l(level, "-- extract value of %s", toXPathNode(element, loopVar));
            body.l(level, "%s := %s.extract('%s/%s/text()', %s);", varTempNode.name(), response.name(), pathPrefix, toXPathNode(element, loopVar),
                    varNsMap.name());
            // IF tempNode IS NOT NULL THEN
            body.l(level, "%s %s %s %s %s %s", ke.ifKey(), varTempNode.name(), ke.is(), ke.not(), ke.nullKey(), ke.then());

            // var := (tempNode.stringVal());
            body.l(level + 1, "%s := %s;", prefix + name,
                    U.stringToBaseType(type.getXsdType().getLocalPart(), varTempNode.name() + ".getStringVal()"));
            // END IF;
            body.l(level, "%s %s;", ke.end(), ke.ifKey());
        }

        return body.toString();
    }

    private String toXPathNode(ElementInfo element, String loopVar)
    {
        return toQName(element) + (loopVar != null ? "[' || " + loopVar + " || ']" : "");
    }

    private String generatePostFunction() throws IOException
    {
        StringBuilder postFunction = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader()
                .getResourceAsStream("post-function-template.txt")));
        while (reader.ready())
        {
            postFunction.append("  " + reader.readLine() + "\n");
        }

        reader.close();

        return postFunction.toString();
    }

}
