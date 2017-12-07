/**
 * BPMN : Link Start Event Shape
 *
 * @class
 * @extends OG.shape.GeomShape
 * @requires OG.common.*
 * @requires OG.geometry.*
 *
 * @param {String} label 라벨 [Optional]
 * @author <a href="mailto:sppark@uengine.org">Seungpil Park</a>
 * @private
 */
OG.shape.bpmn.E_Start_Connector = function (label) {
    OG.shape.bpmn.E_Start_Connector.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.E_Start_Connector';
    this.label = label;

};
OG.shape.bpmn.E_Start_Connector.prototype = new OG.shape.bpmn.E_Start();
OG.shape.bpmn.E_Start_Connector.superclass = OG.shape.bpmn.E_Start;
OG.shape.bpmn.E_Start_Connector.prototype.constructor = OG.shape.bpmn.E_Start_Connector;
OG.E_Start_Connector = OG.shape.bpmn.E_Start_Connector;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Start_Connector.prototype.createShape = function () {
    var geom1, geomCollection = [];
    if (this.geom) {
        return this.geom;
    }

    geom1 = new OG.geometry.Polygon([
        [20, 34],
        [20, 65],
        [60, 65],
        [60, 80],
        [85, 50],
        [60, 20],
        [60, 34]
    ]);

    geomCollection.push(new OG.geometry.Circle([50, 50], 50));
    geomCollection.push(new OG.geometry.Circle([50, 50], 42));
    geomCollection.push(geom1);

    this.geom = new OG.geometry.GeometryCollection(geomCollection);
    this.geom.style = new OG.geometry.Style({
        'label-position': 'bottom'
    });

    return this.geom;
};
