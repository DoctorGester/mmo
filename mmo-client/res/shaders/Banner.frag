#ifdef HAS_ALPHAMAP
	uniform sampler2D m_AlphaMap;
#endif

uniform float g_Time;

varying vec4 pos;
uniform bool m_UseClipping;
uniform vec4 m_Clipping;

uniform sampler2D m_EffectMap;
uniform vec4 m_EffectColor;
uniform bool m_UseEffect;
uniform bool m_EffectFade;
uniform bool m_EffectPulse;
uniform bool m_EffectPulseColor;
uniform bool m_EffectSaturate;
uniform bool m_EffectImageSwap;
uniform bool m_UseEffectTexCoords;
uniform float m_EffectStep;
uniform float m_GlobalAlpha;

uniform vec4 m_Front;
uniform vec4 m_Back;

uniform vec4 m_Color;
uniform sampler2D m_ColorMap;
uniform sampler2D m_Mask;

varying vec2 texCoord1;
varying vec2 texCoord2;
varying vec2 alphaTexCoord;

varying vec4 vertColor;

void main(){
	if (m_UseClipping) {
		if (pos.x < m_Clipping.x || pos.x > m_Clipping.z || 
			pos.y < m_Clipping.y || pos.y > m_Clipping.w) {
			discard;
		}
	}
	
	vec4 color = vec4(1.0);
	vec2 cord = texCoord1;
	
	#ifdef HAS_COLORMAP
		if (m_EffectImageSwap) {
			color *= texture2D(m_ColorMap, texCoord2);
			cord = texCoord2;
		} else {
			color *= texture2D(m_ColorMap, texCoord1);
		}

		vec4 mask = texture2D(m_Mask, cord);

		color += mask * m_Front;
      	/*float a = color.a;
		color.rgb = (m_Front.rgb * a + m_Back.rgb * (1.0 - a)) * mask.rgb;
		color.a = 0.5;*/
		
		if (m_UseEffect) {
			if (m_EffectPulse) {
				color = mix(color, texture2D(m_EffectMap, texCoord2), m_EffectStep);
			} else if (m_EffectFade) {
				color.a *= m_EffectStep;
			} else if (m_EffectPulseColor) {
				color =  mix(color, m_EffectColor, m_EffectStep*0.5);
			} else if (m_EffectSaturate) {
				float intensity = (0.2125 * color.r) + (0.7154 * color.g) + (0.0721 * color.b);
				color = mix(color, vec4(intensity,intensity,intensity,color.a), m_EffectStep);
			} else {
				color = mix(color, texture2D(m_EffectMap, texCoord2), 1.0);
			}
		}
	#endif
	
	#ifdef HAS_COLOR
		color *= m_Color;
	#endif
	
	#ifdef HAS_VERTEXCOLOR
		color *= vertColor;
	#endif
	
	#if defined(HAS_ALPHAMAP)
		color.a *= texture2D(m_AlphaMap, alphaTexCoord).r;
	#endif
	
	color.a *= m_GlobalAlpha;

    gl_FragColor = color;
}