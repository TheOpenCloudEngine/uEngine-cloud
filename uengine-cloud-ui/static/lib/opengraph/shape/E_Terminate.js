/**
 * BPMN : Terminate Event Shape
 *
 * @class
 * @extends OG.shape.bpmn.E_End
 * @requires OG.common.*
 * @requires OG.geometry.*
 *
 * @param {String} label 라벨 [Optional]
 * @author <a href="mailto:sppark@uengine.org">Seungpil Park</a>
 * @private
 */
OG.shape.bpmn.E_Terminate = function (label) {
	OG.shape.bpmn.E_Terminate.superclass.call(this, label);

	this.SHAPE_ID = 'OG.shape.bpmn.E_Terminate';
};
OG.shape.bpmn.E_Terminate.prototype = new OG.shape.bpmn.E_End();
OG.shape.bpmn.E_Terminate.superclass = OG.shape.bpmn.E_End;
OG.shape.bpmn.E_Terminate.prototype.constructor = OG.shape.bpmn.E_Terminate;
OG.E_Terminate = OG.shape.bpmn.E_Terminate;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Terminate.prototype.createShape = function () {
	var geom1, geom2, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom1 = new OG.geometry.Circle([50, 50], 50);
	geom1.style = new OG.geometry.Style({
		"stroke-width": 3
	});

	geom2 = new OG.geometry.Circle([50, 50], 30);
	geom2.style = new OG.geometry.Style({
		"fill"        : "black",
		"fill-opacity": 1
	});

	geomCollection.push(geom1);
	geomCollection.push(geom2);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);
	this.geom.style = new OG.geometry.Style({
		'label-position': 'bottom'
	});

	return this.geom;
};