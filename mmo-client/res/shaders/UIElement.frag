#if defined(HAS_ALPHAMAP)
	uniform sampler2D m_AlphaMap;
#endif

uniform float g_Time;

varying vec4 pos;
#if defined(USE_CLIPPING)
	uniform vec4 m_Clipping;
#endif

uniform float m_GlobalAlpha;

#ifdef IS_TEXTFIELD
//uniform bool m_IsTextField;
//uniform bool m_HasTabFocus;
//uniform bool m_ShowTextRange;
	uniform float m_CaretX;
	uniform float m_CaretSpeed;
	uniform float m_LastUpdate;
	uniform float m_TextRangeStart;
	uniform float m_TextRangeEnd;
#endif

uniform vec4 m_Color;
uniform sampler2D m_ColorMap;

varying vec2 texCoord1;

#if defined(BLEND)
	varying vec2 texCoord2;
	uniform float m_BlendStep;
#endif

#if defined(HAS_ALPHAMAP)
	varying vec2 alphaTexCoord;
#endif

//vec3 altMix(in vec3 color1, in vec3 color2, in float alpha) {
//	return (color1.rgb * vec3(1.0-alpha) + color2.rgb * vec3(alpha));
//}

void main(){
	#if defined(USE_CLIPPING)
		if (pos.x < m_Clipping.x || pos.x > m_Clipping.z || 
			pos.y < m_Clipping.y || pos.y > m_Clipping.w) {
			discard;
		}
	#endif
	
	vec4 color = texture2D(m_ColorMap, texCoord1);

	#if defined(BLEND)
		color = mix(color, texture2D(m_ColorMap, texCoord2), m_BlendStep);
	#endif

	#ifdef HAS_COLOR
		color *= m_Color;
	#endif

	#if defined(IS_TEXTFIELD)
		#if defined(SHOW_TEXT_RANGE)
			float trStart;
			float trEnd;
			if (m_TextRangeStart < m_TextRangeEnd) {
				trStart = m_TextRangeStart;
				trEnd = m_TextRangeEnd;
			} else {
				trStart = m_TextRangeEnd;
				trEnd = m_TextRangeStart;
			}
			if (pos.x >= trStart && pos.x <= trEnd) {
				color = vec4(0.0,0.0,1.0,0.5);
			}
		#endif
		#if defined(HAS_TAB_FOCUS)
			if (g_Time-m_LastUpdate > 0.25) {
				if (pos.x > m_CaretX-1.0 && pos.x < m_CaretX+1.0) {
					color = m_Color;
					color.a = sin((g_Time-m_LastUpdate)*m_CaretSpeed);
				} else {
					if (color == m_Color)
						color = vec4(0.0);
				}
			} else {
				if (pos.x > m_CaretX-1.0 && pos.x < m_CaretX+1.0) {
					color = m_Color;
				} else {
					if (color == m_Color)
						color = vec4(0.0);
				}
			}
		#else
			color.a = 0.0;
		#endif
	#endif
	
	#if defined(HAS_ALPHAMAP)
		color.a *= texture2D(m_AlphaMap, alphaTexCoord).r;
	#endif
	
	color.a *= m_GlobalAlpha;
	
    gl_FragColor = color;
}
