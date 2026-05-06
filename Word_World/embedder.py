# -*- coding: utf-8 -*-
"""
LatentSpace Explorer - Python Embedder
Loads GloVe model, runs PCA, outputs full_vectors.json and pca_vectors.json
"""

import json
import gensim.downloader as api
from sklearn.decomposition import PCA
import numpy as np
import sys

# ניתן להעביר מספר מילים כארגומנט, ברירת מחדל 5000
limit = int(sys.argv[1]) if len(sys.argv) > 1 else 5000

# מודל GloVe עם 100 ממדים
gensim_model_name = "glove-wiki-gigaword-100"
print(f"--- Loading GloVe model (this may take a minute on first run)... ---", flush=True)
model = api.load(gensim_model_name)

# ניקח רק את X המילים הנפוצות ביותר
words = model.index_to_key[:limit]
full_vectors = [model[word].tolist() for word in words]

print(f"--- Performing PCA (100D -> 50D)... ---", flush=True)
# ביצוע PCA ל-50 רכיבים
# תיקון: fit_transform (לא fit_with_rotations שאינה קיימת ב-sklearn)
pca = PCA(n_components=50)
pca_result = pca.fit_transform(np.array(full_vectors))

# הכנת מבני הנתונים ל-JSON
full_space_data = []
pca_space_data = []

for i, word in enumerate(words):
    full_space_data.append({
        "word": word,
        "vector": full_vectors[i]
    })
    pca_space_data.append({
        "word": word,
        "vector": pca_result[i].tolist()
    })

print(f"--- Saving files... ---", flush=True)

with open('full_vectors.json', 'w', encoding='utf-8') as f:
    json.dump(full_space_data, f, ensure_ascii=False)

with open('pca_vectors.json', 'w', encoding='utf-8') as f:
    json.dump(pca_space_data, f, ensure_ascii=False)

print(f"--- Success! Created 'full_vectors.json' and 'pca_vectors.json' ---", flush=True)
print(f"--- Number of words: {len(words)} ---", flush=True)
print(f"--- PCA explained variance (first 5): {pca.explained_variance_ratio_[:5].tolist()} ---", flush=True)
