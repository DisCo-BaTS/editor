package modelspaceinterface;

import handlers.ElementHandler;
import library.model.dto.scenario.ScenarioDTO;
import library.services.scenario.ScenarioConverter;
import model.InheretedClass;
import model.InstanciatedClass;
import model.ModelAttribute;
import model.ModelElement;
import org.junit.jupiter.api.Test;
import xml.XMLProfileReader;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class ScenarioGeneratorTest {

    @Test
    void generateScenario() throws JAXBException, IOException {
        ScenarioDTO scenario = ScenarioGenerator.generateScenario(getTestScenario());

        ScenarioConverter.convertToXML(scenario, "C:\\Users\\alex\\Desktop", "generatedScenario");
    }

    private ElementHandler getTestScenario() {
        XMLProfileReader xmlProfileReader = new XMLProfileReader();
        ElementHandler elementHandler = ElementHandler.getInstance();
        elementHandler.setXMLData(xmlProfileReader.readXML());

        InheretedClass trafficParticipant = elementHandler.extendClass("DynamicSimulationObject", "TrafficParticipant");
        ModelAttribute width = new ModelAttribute();
        width.id = ElementHandler.getID();
        width.name = "width";
        width.type = "SimulationProperty<Double>";
        width.setDefaultValue("48.0");
        width.visibility = "private";
        trafficParticipant.attributes.put(width.id, width);
        ModelAttribute length = new ModelAttribute();
        length.id = ElementHandler.getID();
        length.name = "length";
        length.type = "SimulationProperty<Double>";
        length.setDefaultValue("367.0");
        length.visibility = "private";
        trafficParticipant.attributes.put(length.id, length);

        InheretedClass ship = elementHandler.extendClass("TrafficParticipant", "Ship");
        ModelAttribute speed = new ModelAttribute();
        speed.id = ElementHandler.getID();
        speed.name = "speed";
        speed.type = "SimulationProperty<Double>";
        speed.minMultiplicity = 0;
        speed.maxMultiplicity = 1;
        speed.visibility = "private";
        ship.attributes.put(speed.id, speed);

        InheretedClass cargoShip = elementHandler.extendClass("Ship", "CargoShip");
        ModelAttribute name = new ModelAttribute();
        name.id = ElementHandler.getID();
        name.name = "name";
        name.type = "SimulationProperty<String>";
        name.visibility = "private";
        cargoShip.attributes.put(name.id, name);
        ModelAttribute mmsi = new ModelAttribute();
        mmsi.id = ElementHandler.getID();
        mmsi.name = "mmsi";
        mmsi.type = "SimulationProperty<String>";
        mmsi.visibility = "private";
        cargoShip.attributes.put(mmsi.id, mmsi);
        ModelAttribute heading = new ModelAttribute();
        heading.id = ElementHandler.getID();
        heading.name = "heading";
        heading.type = "SimulationProperty<Integer>";
        heading.setDefaultValue("180");
        heading.visibility = "private";
        cargoShip.attributes.put(heading.id, heading);
        ModelAttribute csInterObjectCommunication = new ModelAttribute();
        csInterObjectCommunication.id = ElementHandler.getID();
        csInterObjectCommunication.name = "interObjectCommunication";
        csInterObjectCommunication.type = "ArrayList<InterObjectCommunication>";
        csInterObjectCommunication.visibility = "private";
        csInterObjectCommunication.maxMultiplicity = -1;
        cargoShip.attributes.put(csInterObjectCommunication.id, csInterObjectCommunication);

        InheretedClass obstacle = elementHandler.extendClass("SimulationObject", "Obstacle");

        InheretedClass lighthouse = elementHandler.extendClass("Obstacle", "Lighthouse");
        ModelAttribute height = new ModelAttribute();
        height.id = ElementHandler.getID();
        height.name = "height";
        height.type = "SimulationProperty<Double>";
        lighthouse.attributes.put(height.id, heading);
        ModelAttribute lhInterObjectCommunication = new ModelAttribute();
        lhInterObjectCommunication.id = ElementHandler.getID();
        lhInterObjectCommunication.name = "interObjectCommunication";
        lhInterObjectCommunication.type = "ArrayList<InterObjectCommunication>";
        lhInterObjectCommunication.visibility = "private";
        lhInterObjectCommunication.maxMultiplicity = -1;
        lighthouse.attributes.put(lhInterObjectCommunication.id, lhInterObjectCommunication);

        InheretedClass positioningComponent = elementHandler.extendClass("SimulationComponent", "PositioningComponent");

        InstanciatedClass hamburgExpress = new InstanciatedClass(cargoShip);
        elementHandler.addInstance(hamburgExpress);
        HashMap<String, Object> nameProp = new HashMap<>();
        nameProp.put("value", "HamburgExpress");
        nameProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        nameProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        hamburgExpress.setNamedAttribute("name", nameProp, null);
        HashMap<String, Object> mmsiProp = new HashMap<>();
        mmsiProp.put("value", "218774000");
        mmsiProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        mmsiProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        hamburgExpress.setNamedAttribute("mmsi", mmsiProp, null);
        HashMap<String, Object> positionProp = new HashMap<>();
        HashMap<String, Object> position = new HashMap<>();
        position.put("latitude", 54.526461);
        position.put("longitude", 5.475241);
        positionProp.put("value", position);
        positionProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        positionProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        hamburgExpress.setNamedAttribute("position", positionProp, null);
        HashMap<String, Object> speedProp = new HashMap<>();
        speedProp.put("value", 10.0);
        speedProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        speedProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        hamburgExpress.setNamedAttribute("speed", speedProp, null);
        HashMap<String, Object> headingProp = new HashMap<>();
        headingProp.put("value", 180);
        headingProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        headingProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        hamburgExpress.setNamedAttribute("heading", headingProp, null);

        InstanciatedClass wittenbergen = new InstanciatedClass(lighthouse);
        elementHandler.addInstance(wittenbergen);
        HashMap<String, Object> lNameProperty = new HashMap<>();
        lNameProperty.put("value", "Lgihthouse Wittenbergen");
        lNameProperty.put("simulationVisibility", "SimulationVisibilityKind.READ");
        lNameProperty.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        wittenbergen.setNamedAttribute("name", lNameProperty, null);
        HashMap<String, Object> lheight = new HashMap<>();
        lheight.put("value", 30.0);
        lheight.put("simulationVisibility", "SimulationVisibilityKind.READ");
        lheight.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        wittenbergen.setNamedAttribute("height", lheight, null);
        HashMap<String, Object> lPositionProp = new HashMap<>();
        HashMap<String, Object> lPosition = new HashMap<>();
        lPosition.put("latitude", 54.536461);
        lPosition.put("longitude", 5.475241);
        lPositionProp.put("value", lPosition);
        lPositionProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        lPositionProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        wittenbergen.setNamedAttribute("position", lPositionProp, null);
        HashMap<String, Object> shipPositions = new HashMap<>();
        shipPositions.put("simulationVisibility", "SimulationVisibilityKind.WRITE");
        shipPositions.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        wittenbergen.setNamedAttribute("shipPositions", shipPositions, null);

        ModelElement ioc = elementHandler.getNamedElement("InterObjectCommunication");
        InstanciatedClass positionExchange = new InstanciatedClass(ioc);
        elementHandler.addInstance(positionExchange);
        ArrayList<Object> n = new ArrayList<>();
        n.add(hamburgExpress.getNamedAttribute("position"));
        positionExchange.setNamedAttribute("communicatedProperties", n, null);
        positionExchange.setNamedAttribute("end", wittenbergen, null);
        positionExchange.setNamedAttribute("start", hamburgExpress, null);

        ArrayList<Object> interObjectCommList1 = new ArrayList<>();
        interObjectCommList1.add(positionExchange);
        ArrayList<Object> interObjectCommList2 = new ArrayList<>();
        interObjectCommList2.add(positionExchange);
        hamburgExpress.setNamedAttribute("interObjectCommunication", interObjectCommList1, null);
        wittenbergen.setNamedAttribute("interObjectCommunication", interObjectCommList2, null);

        ModelElement simComponent = elementHandler.getNamedElement("PositioningComponent");
        ModelElement identifier = elementHandler.getNamedElement("Identifier");
        ModelElement methodCall = elementHandler.getNamedElement("MethodCall");
        InstanciatedClass positioningComponentIns = new InstanciatedClass(simComponent);
        elementHandler.addInstance(positioningComponentIns);
        InstanciatedClass posCompIdent = new InstanciatedClass(identifier);
        elementHandler.addInstance(posCompIdent);
        posCompIdent.setNamedAttribute("path", "simplePos::step", null);
        InstanciatedClass posCompMethodCall = new InstanciatedClass(methodCall);
        elementHandler.addInstance(posCompMethodCall);
        posCompMethodCall.setNamedAttribute("path", posCompIdent, null);
        ArrayList<AttributeReference> mcParameters = new ArrayList<>();
        mcParameters.add(new AttributeReference((HashMap<String, Object>) hamburgExpress.getNamedAttribute("position")));
        mcParameters.add(new AttributeReference((HashMap<String, Object>) hamburgExpress.getNamedAttribute("speed")));
        mcParameters.add(new AttributeReference((HashMap<String, Object>) hamburgExpress.getNamedAttribute("heading")));
        posCompMethodCall.setNamedAttribute("methodParameters", mcParameters, null);
        positioningComponentIns.setNamedAttribute("linkedMethodCall", posCompMethodCall, null);
        ArrayList<Object> components = new ArrayList<>();
        components.add(positioningComponentIns);
        hamburgExpress.setNamedAttribute("components", components, null);

        return elementHandler;
    }
}