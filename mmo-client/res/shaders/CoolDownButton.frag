#define M_PI 3.1415926535897932384626433832795

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

uniform float m_CoolDownLeft;
uniform bool m_Pressed;
uniform bool m_Glowing;
uniform bool m_Active;

uniform vec4 m_Color;
uniform sampler2D m_ColorMap;

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

	float pPart = -m_CoolDownLeft * M_PI - M_PI / 2.0;
	float angle = atan(cord.y - 0.5, cord.x - 0.5);

	if ((angle > M_PI / 2.0 && angle < M_PI) || m_CoolDownLeft < -0.98){
		pPart = M_PI / 2.0 + pPart;
		angle = angle - M_PI - M_PI / 2.0;
	}

	if (m_CoolDownLeft < 1.0){
		color.r = min(1.0, color.r + 0.15);
	}

	if (m_CoolDownLeft > 0.75 && m_CoolDownLeft < 1.0){
		float power = cos(((1.0 - m_CoolDownLeft) / 0.25) * M_PI - M_PI / 2.0) / 2.0;
		color.r = min(1.0, color.r + power);
		color.g = min(1.0, color.g + power);
	}

	if (angle < pPart || m_Pressed){
		color.rgb *= 0.35;
	}

	if (!m_Active){
		color.rgb *= 0.65;
	}

	if (m_Glowing){
		color.r = min(color.r * 1.55, 1.0);
		color.g = min(color.g * 1.55, 1.0);
		color.b = min(color.b * 1.55, 1.0);
	}

    gl_FragColor = color;
}