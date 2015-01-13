#define M_PI 3.1415926535897932384626433832795
#define M_2PI 6.283185307179586

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

uniform vec4 m_Color;
uniform sampler2D m_ColorMap;

uniform vec4 m_Colors[];
uniform int m_PlayerAmount;
uniform int m_Turning;
uniform float m_Step;

varying vec2 texCoord1;
varying vec2 texCoord2;
varying vec2 alphaTexCoord;

varying vec4 vertColor;

#define RAD_INNER 0.15
#define RAD_OUTER 0.25

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

	vec2 offs = cord - vec2(0.5, 0.5);

	float an = atan(offs.y, offs.x);
	float dist = sqrt(offs.x * offs.x + offs.y * offs.y);

	vec4 Colors[4];
	Colors[0] = vec4(1.0, 0.0, 0.0, 1.0);
	Colors[1] = vec4(0.0, 1.0, 0.0, 1.0);
	Colors[2] = vec4(0.0, 1.0, 1.0, 1.0);
	Colors[3] = vec4(1.0, 1.0, 0.0, 1.0);

	int PlayerAmount = 4;
	int Turning = 0;
	float Step = 0.0;

	if (an < 0)
		an = 2 * M_PI + an;

	if (dist > RAD_INNER && dist < RAD_OUTER){
		float piece = M_2PI / float(PlayerAmount);

		for(int i = 0; i < PlayerAmount; i++){
			float begin = mod(i * piece + piece / PlayerAmount, M_2PI);
			float end = mod((i + 1) * piece + piece / PlayerAmount, M_2PI);

			if (an > begin || (an < end && begin > M_2PI - piece)){
				color = Colors[i];
				color = color - vec4((RAD_OUTER - dist) / (RAD_OUTER - RAD_INNER));
			}
		}
	}

    gl_FragColor = color;
}