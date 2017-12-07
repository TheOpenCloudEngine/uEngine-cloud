OG.shape.bpmn.Value_Chain_Module = function (label) {
    OG.shape.bpmn.Value_Chain_Module.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.Value_Chain_Module';
    this.label = label;
    this.HaveButton = true;
    this.LoopType = "None";
    this.inclusion = false;

};
OG.shape.bpmn.Value_Chain_Module.prototype = new OG.shape.bpmn.Value_Chain();
OG.shape.bpmn.Value_Chain_Module.superclass = OG.shape.bpmn.Value_Chain;
OG.shape.bpmn.Value_Chain_Module.prototype.constructor = OG.shape.bpmn.Value_Chain_Module;
OG.Value_Chain_Module = OG.shape.bpmn.Value_Chain_Module;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.Value_Chain_Module.prototype.createShape = function () {
    if (this.geom) {
        return this.geom;
    }

    this.geom = new OG.geometry.Polygon([
        [0, 0],
        [0, 100],
        [90, 100],
        [100, 50],
        [90, 0]
    ]);

    this.geom.style = new OG.geometry.Style({
        fill: "#FFFFFF-#9FD7FF",
        "fill-opacity": 1,
        "stroke": '#9FD7FF'
    });

    return this.geom;
};

OG.shape.bpmn.Value_Chain.prototype.createSubShape = function () {
    this.sub = [];

    if (this.inclusion) {
        this.sub.push({
            shape: new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + 'complete.png'),
            width: '20px',
            height: '20px',
            right: '0px',
            bottom: '20px',
            style: {}
        })
    }

    if (this.HaveButton) {
        this.sub.push({
            shape: new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + 'subprocess.png'),
            width: '20px',
            height: '20px',
            align: 'center',
            bottom: '5px',
            style: {
                "stroke-width": 1,
                fill: "white",
                "fill-opacity": 0,
                "shape-rendering": "crispEdges"
            }
        })
    }

    return this.sub;
};