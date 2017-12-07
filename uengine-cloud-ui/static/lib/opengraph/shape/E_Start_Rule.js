/**
 * BPMN : Rule Start Event Shape
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
OG.shape.bpmn.E_Start_Rule = function (label) {
	OG.shape.bpmn.E_Start_Rule.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.E_Start_Rule';
	this.label = label;
};
OG.shape.bpmn.E_Start_Rule.prototype = new OG.shape.bpmn.E_Start();
OG.shape.bpmn.E_Start_Rule.superclass = OG.shape.bpmn.E_Start;
OG.shape.bpmn.E_Start_Rule.prototype.constructor = OG.shape.bpmn.E_Start_Rule;
OG.E_Start_Rule = OG.shape.bpmn.E_Start_Rule;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Start_Rule.prototype.createShape = function () {
	var geom1, geom2, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom1 = new OG.geometry.Circle([50, 50], 50);
	geom1.style = new OG.geometry.Style({
		"stroke-width": 1.5
	});

	geom2 = new OG.geometry.Rectangle([25, 20], 50, 60);

	geomCollection.push(geom1);
	geomCollection.push(geom2);
	geomCollection.push(new OG.geometry.Line([30, 30], [70, 30]));
	geomCollection.push(new OG.geometry.Line([30, 45], [70, 45]));
	geomCollection.push(new OG.geometry.Line([30, 60], [70, 60]));
	geomCollection.push(new OG.geometry.Line([30, 70], [70, 70]));

	this.geom = new OG.geometry.GeometryCollection(geomCollection);
	this.geom.style = new OG.geometry.Style({
		'label-position': 'bottom'
	});

	return this.geom;
};
