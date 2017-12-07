/**
 * BPMN : Cancel End Event Shape
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
OG.shape.bpmn.E_End_Cancel = function (label) {
    OG.shape.bpmn.E_End_Cancel.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.E_End_Cancel';
    this.label = label;
};
OG.shape.bpmn.E_End_Cancel.prototype = new OG.shape.bpmn.E_End();
OG.shape.bpmn.E_End_Cancel.superclass = OG.shape.bpmn.E_End;
OG.shape.bpmn.E_End_Cancel.prototype.constructor = OG.shape.bpmn.E_End_Cancel;
OG.E_End_Cancel = OG.shape.bpmn.E_End_Cancel;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_End_Cancel.prototype.createShape = function () {
    var geom1, geom2, geom3, geomCollection = [];
    if (this.geom) {
        return this.geom;
    }

    geom1 = new OG.geometry.Circle([50, 50], 50);
    geom1.style = new OG.geometry.Style({
        "stroke-width": 3
    });

    geom2 = new OG.geometry.Line([25, 25], [75, 75]);
    geom2.style = new OG.geometry.Style({
        "stroke-width": 5
    });

    geom3 = new OG.geometry.Line([25, 75], [75, 25]);
    geom3.style = new OG.geometry.Style({
        "stroke-width": 5
    });

    geomCollection.push(geom1);
    geomCollection.push(geom2);
    geomCollection.push(geom3);

    this.geom = new OG.geometry.GeometryCollection(geomCollection);
    this.geom.style = new OG.geometry.Style({
        'label-position': 'bottom'
    });

    return this.geom;
};
