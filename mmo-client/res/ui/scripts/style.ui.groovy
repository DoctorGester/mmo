import com.jme3.font.BitmapFont
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.texture.Texture
import com.simsilica.lemur.*;
import com.simsilica.lemur.Button.ButtonAction;
import com.simsilica.lemur.component.*
import com.simsilica.lemur.style.Attributes;

Attributes.metaClass {
	configure { Closure c ->

		if (c != null) {
			// If we don't set things up this way then
			// we end up with statements like:
			//         foo=3
			// Setting a higher up property instead of the
			// current object we are configuring.
			c.setResolveStrategy(DELEGATE_FIRST);
			c.setDelegate(delegate);
			c();
		}
		return delegate;
	}
}

Attributes selector(String style, Closure c) {
	def attrs = styles.getSelector(style);
	attrs.configure(c)
	return attrs;
}

Attributes selector(String id, String style, Closure c) {
	def attrs = styles.getSelector(id, style);
	attrs.configure(c)
	return attrs;
}

Attributes selector(String parent, String child, String style, Closure c) {
	def attrs = styles.getSelector(parent, child, style);
	attrs.configure(c)
	return attrs;
}

BitmapFont font(String name) {
	return gui.loadFont(name)
}

ColorRGBA color(Number r, Number g, Number b, Number a) {
	return new ColorRGBA(r.floatValue(), g.floatValue(), b.floatValue(), a.floatValue())
}

Texture texture(String name) {
	return gui.loadTexture(name, true, true);
}

Texture texture(Map args) {
	String name = args.name;
	if (name == null) {
		throw new IllegalArgumentException("Texture name not specified.");
	}

	boolean generateMips = args.generateMips != Boolean.FALSE
	Texture t = gui.loadTexture(name, true, generateMips)
	for (Map.Entry e : args) {
		if (e.key == "name" || e.key == "generateMips")
			continue;
		t[e.key] = e.value
	}

	return t;
}

Vector3f vec3(Number x, Number y, Number z) {
	return new Vector3f(x.floatValue(), y.floatValue(), z.floatValue());
}

Vector2f vec2(Number x, Number y) {
	return new Vector2f(x.floatValue(), y.floatValue());
}

def gradient = TbtQuadBackgroundComponent.create(
		texture(name: "/com/simsilica/lemur/icons/bordered-gradient.png",
				generateMips: false),
		1, 1, 1, 126, 126,
		1f, false);

def bevel = TbtQuadBackgroundComponent.create(
		texture(name: "/com/simsilica/lemur/icons/bevel-quad.png",
				generateMips: false),
		0.125f, 8, 8, 119, 119,
		1f, false);

def border = TbtQuadBackgroundComponent.create(
		texture(name: "/com/simsilica/lemur/icons/border.png",
				generateMips: false),
		1, 1, 1, 6, 6,
		1f, false);
def border2 = TbtQuadBackgroundComponent.create(
		texture(name: "/com/simsilica/lemur/icons/border.png",
				generateMips: false),
		1, 2, 2, 6, 6,
		1f, false);

def doubleGradient = new QuadBackgroundComponent(color(0.5, 0.75, 0.85, 0.5));
doubleGradient.texture = texture(name: "/com/simsilica/lemur/icons/double-gradient-128.png",
		generateMips: false)

selector("glass") {
	fontSize = 14
}

selector("label", "glass") {
	insets = new Insets3f(2, 2, 0, 2);
	color = color(0.5, 0.75, 0.75, 0.85)
}

selector("container", "glass") {
	background = gradient.clone()
	background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector("slider", "glass") {
	background = gradient.clone()
	background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

def pressedCommand = new Command<Button>() {
	public void execute(Button source) {
		if (source.isPressed()) {
			source.move(1, -1, 0);
		} else {
			source.move(-1, 1, 0);
		}
	}
};

def stdButtonCommands = [
		(ButtonAction.Down): [pressedCommand],
		(ButtonAction.Up)  : [pressedCommand]
];

selector("title", "glass") {
	color = color(0.8, 0.9, 1, 0.85f)
	highlightColor = color(1, 0.8, 1, 0.85f)
	shadowColor = color(0, 0, 0, 0.75f)
	shadowOffset = new com.jme3.math.Vector3f(2, -2, 1);
	background = new QuadBackgroundComponent(color(0.5, 0.75, 0.85, 0.5));
	background.texture = texture(name: "/com/simsilica/lemur/icons/double-gradient-128.png",
			generateMips: false)
	insets = new Insets3f(2, 2, 2, 2);

	buttonCommands = stdButtonCommands;
}


selector("button", "glass") {
	background = gradient.clone()
	color = color(0.8, 0.9, 1, 0.85f)
	background.setColor(color(0, 0.75, 0.75, 0.5))
	insets = new Insets3f(2, 2, 2, 2);

	buttonCommands = stdButtonCommands;
}

selector("slider", "glass") {
	insets = new Insets3f(1, 3, 1, 2);
}

selector("slider", "button", "glass") {
	background = doubleGradient.clone()
	background.setColor(color(0.5, 0.75, 0.75, 0.5))
	insets = new Insets3f(0, 0, 0, 0);
}

selector("slider.thumb.button", "glass") {
	text = "  "
	color = color(0.6, 0.8, 0.8, 0.85)
}

selector("slider.left.button", "glass") {
	text = "-"
	background = doubleGradient.clone()
	background.setColor(color(0.5, 0.75, 0.75, 0.5))
	background.setMargin(5, 0);
	color = color(0.6, 0.8, 0.8, 0.85)
}

selector("slider.right.button", "glass") {
	text = "+"
	background = doubleGradient.clone()
	background.setColor(color(0.5, 0.75, 0.75, 0.5))
	background.setMargin(4, 0);
	color = color(0.6, 0.8, 0.8, 0.85)
}

selector("checkbox", "glass") {
	def on = new IconComponent("/com/simsilica/lemur/icons/Glass-check-on.png", 1f,
			0, 0, 1f, false);
	on.setColor(color(0.5, 0.9, 0.9, 0.9))
	on.setMargin(5, 0);
	def off = new IconComponent("/com/simsilica/lemur/icons/Glass-check-off.png", 1f,
			0, 0, 1f, false);
	off.setColor(color(0.6, 0.8, 0.8, 0.8))
	off.setMargin(5, 0);

	onView = on;
	offView = off;

	color = color(0.8, 0.9, 1, 0.85f)
}

selector("rollup", "glass") {
	background = gradient.clone()
	background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector("tabbedPanel", "glass") {
	activationColor = color(0.8, 0.9, 1, 0.85f)
}

selector("tabbedPanel.container", "glass") {
	background = null
}

selector("tab.button", "glass") {
	background = gradient.clone()
	background.setColor(color(0.25, 0.5, 0.5, 0.5))
	color = color(0.4, 0.45, 0.5, 0.85f)
	insets = new Insets3f(4, 2, 0, 2);

	buttonCommands = stdButtonCommands;
}


