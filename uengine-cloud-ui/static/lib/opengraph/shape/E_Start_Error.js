/**
 * BPMN : Error Start Event Shape
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
OG.shape.bpmn.E_Start_Error = function (label) {
    OG.shape.bpmn.E_Start_Error.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.E_Start_Error';
    this.label = label;
};
OG.shape.bpmn.E_Start_Error.prototype = new OG.shape.bpmn.E_Start();
OG.shape.bpmn.E_Start_Error.superclass = OG.shape.bpmn.E_Start;
OG.shape.bpmn.E_Start_Error.prototype.constructor = OG.shape.bpmn.E_Start_Error;
OG.E_Start_Error = OG.shape.bpmn.E_Start_Error;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Start_Error.prototype.createShape = function () {
    var geom1, geom2, geomCollection = [];
    if (this.geom) {
        return this.geom;
    }

    geom1 = new OG.geometry.Circle([50, 50], 50);

    geom2 = new OG.geometry.Polygon([
        [20, 75],
        [40, 30],
        [60, 60],
        [80, 20],
        [60, 75],
        [40, 50]
    ]);

    geomCollection.push(geom1);
    geomCollection.push(geom2);

    this.geom = new OG.geometry.GeometryCollection(geomCollection);
    this.geom.style = new OG.geometry.Style({
        'label-position': 'bottom',
        "stroke-width" : 1.5
    });

    return this.geom;
};
