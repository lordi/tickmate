import sys

import matplotlib
import matplotlib.pyplot as plt

from tmkit import TickmateDatabase

matplotlib.style.use('ggplot')

tm = TickmateDatabase(sys.argv[1])
ignore_ids = (7,)

for track_id, track in tm.tracks.iterrows():
    if not track_id in ignore_ids:
        tm.ticks[track_id]\
            .resample('M', how='sum')\
            .fillna(value=0)\
            .plot(label=track['name'], linewidth=2, legend=True)

plt.show()

