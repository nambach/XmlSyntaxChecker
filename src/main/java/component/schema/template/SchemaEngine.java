package component.schema.template;

import org.apache.xerces.impl.xs.XSImplementationImpl;
import org.apache.xerces.xs.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import java.util.HashMap;
import java.util.Map;

import static org.apache.xerces.xs.XSConstants.*;


public class SchemaEngine {

    private static Map<Short, String> compositors = new HashMap<>();
    private static Map<Short, String> attrType = new HashMap<>();
    private static Map<Short, String> builtinTypes = new HashMap<>();

    static {
        compositors.put(XSModelGroup.COMPOSITOR_SEQUENCE, Element.INDICATOR.SEQUENCE);
        compositors.put(XSModelGroup.COMPOSITOR_CHOICE, Element.INDICATOR.CHOICE);
        compositors.put(XSModelGroup.COMPOSITOR_ALL, Element.INDICATOR.ALL);

        attrType.put(VC_DEFAULT, "default");
        attrType.put(VC_FIXED, "fixed");
        attrType.put(VC_NONE, "none");

        builtinTypes.put(INTEGER_DT, "integer");
        builtinTypes.put(INT_DT, "int");
        builtinTypes.put(STRING_DT, "string");
        builtinTypes.put(FLOAT_DT, "float");
    }

    public static Element getRootElement(String xsdPath) {
        XSModel model = getSchemaModel(xsdPath);
        if (model == null) {
            return null;
        }

        XSObject root = model.getComponents(ELEMENT_DECLARATION).item(0);
        XSElementDeclaration declaration = (XSElementDeclaration) root;

        Element rootElement = new Element(Element.TYPE.ELEMENT_ONLY, declaration.getName(), null);
        iterate(rootElement, declaration.getTypeDefinition());

        return rootElement;
    }

    private static void iterate(Element current, XSTypeDefinition type) {

        switch (type.getTypeCategory()) {
            case XSTypeDefinition.SIMPLE_TYPE:
                //displaySimpleType(current, element);
                current.setType(Element.TYPE.TEXT_ONLY);
                break;
            case XSTypeDefinition.COMPLEX_TYPE:
                displayComplexType(current, type);
                break;
        }
    }

    private static void displaySimpleType(Element current, XSTypeDefinition typeDefinition) {
//        XSSimpleTypeDefinition simpleTypeDefinition = (XSSimpleTypeDefinition) typeDefinition;
//        String type = builtinTypes.get(simpleTypeDefinition.getBuiltInKind());
    }

    private static void displayComplexType(Element current, XSTypeDefinition typeDefinition) {
        XSComplexTypeDefinition complexTypeDefinition = (XSComplexTypeDefinition) typeDefinition;

        switch (complexTypeDefinition.getContentType()) {
            case XSComplexTypeDefinition.CONTENTTYPE_EMPTY:
                current.setType(Element.TYPE.EMPTY);
                break;
            case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE:
                current.setType(Element.TYPE.TEXT_ONLY);
                break;
            case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
                current.setType(Element.TYPE.ELEMENT_ONLY);
                analyzeParticle(current, complexTypeDefinition.getParticle());
                break;
            case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
                current.setType(Element.TYPE.MIXED);
                analyzeParticle(current, complexTypeDefinition.getParticle());
                break;
        }
        displayAttributes(current, complexTypeDefinition.getAttributeUses());
    }

    private static void displayAttributes(Element current, XSObjectList attrList) {
        for (int i = 0; i < attrList.getLength(); i++) {
            XSObject xsObject = attrList.item(i);
            XSAttributeUse attributeUse = (XSAttributeUse) xsObject;

            String name = attributeUse.getAttrDeclaration().getName();
            boolean required = attributeUse.getRequired();
            String defaultValue = attributeUse.getConstraintValue();

            current.addAttribute(new Attribute(name, defaultValue, required));
        }
    }

    private static void analyzeParticle(Element current, XSParticle particle) {
        if (particle.getTerm().getType() == MODEL_GROUP) {
            XSModelGroup modelGroup = (XSModelGroup) particle.getTerm();

            String indicator = compositors.get(modelGroup.getCompositor());
            current.setInnerType(indicator);

            XSObjectList list = modelGroup.getParticles();
            for (int i = 0; i < list.getLength(); i++) {
                XSParticle particleItem = (XSParticle) list.item(i);

                switch (particleItem.getTerm().getType()) {
                    case ELEMENT_DECLARATION:
                        XSElementDeclaration declaration = (XSElementDeclaration) particleItem.getTerm();

                        Element innerElement = new Element(null, declaration.getName(), current);
                        innerElement.setMin(particleItem.getMinOccurs());
                        innerElement.setMax(particleItem.getMaxOccurs());
                        innerElement.setUnbounded(particleItem.getMaxOccursUnbounded());

                        current.addChildElement(innerElement);

                        iterate(innerElement, declaration.getTypeDefinition());
                        break;
                }
            }
        }
    }

    private static XSModel getSchemaModel(String path) {
        try {
            System.setProperty(DOMImplementationRegistry.PROPERTY,
                    "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

            XSImplementation impl =
                    (XSImplementation) registry.getDOMImplementation("XS-Loader");
            XSLoader schemaLoader = impl.createXSLoader(null);

            return schemaLoader.loadURI(path);
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
