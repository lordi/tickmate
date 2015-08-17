import sys

import matplotlib
import matplotlib.pyplot as plt
import pandas as pd

from tmkit import TickmateDatabase

matplotlib.style.use('ggplot')

tm = TickmateDatabase(sys.argv[1], hide_names=True)
ignore_ids = ()

for track_id, track in tm.tracks.iterrows():
    if not track_id in ignore_ids:
        pd.rolling_mean(tm.timeseries[track_id]\
            .resample('M', how='sum'), 4)\
            .plot(label=track['name'], linewidth=2, legend=True)

plt.show()

