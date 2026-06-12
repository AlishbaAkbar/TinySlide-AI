import streamlit as st
import re
import json

st.set_page_config(page_title="TinySlide AI", layout="wide")

st.title("TinySlide AI")
st.subheader("Offline Text-to-Slide Generator")

text = st.text_area("Paste your notes here:", height=250)

def clean_sentences(text):
    sentences = re.split(r'(?<=[.!?]) +', text.strip())
    return [s.strip() for s in sentences if len(s.strip()) > 10]

def generate_title(text):
    words = text.split()
    return " ".join(words[:6]).title() if words else "Untitled Presentation"

def make_slides(text):
    sentences = clean_sentences(text)
    title = generate_title(text)

    slides = []
    chunk_size = 3

    for i in range(0, len(sentences), chunk_size):
        chunk = sentences[i:i+chunk_size]

        slide = {
            "heading": f"Slide {len(slides)+1}",
            "bullets": chunk,
            "layout": "title_and_bullets"
        }

        slides.append(slide)

    return {
        "title": title,
        "slides": slides
    }

if st.button("Generate Slides"):
    if text.strip():
        result = make_slides(text)

        col1, col2 = st.columns(2)

        with col1:
            st.subheader("Generated JSON")
            st.json(result)

        with col2:
            st.subheader("Slide Preview")
            st.markdown(f"# {result['title']}")

            for slide in result["slides"]:
                st.markdown("---")
                st.markdown(f"## {slide['heading']}")
                for bullet in slide["bullets"]:
                    st.markdown(f"- {bullet}")

    else:
        st.warning("Please enter some text.")