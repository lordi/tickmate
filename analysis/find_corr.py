from __future__ import print_function
import sys

import matplotlib
import matplotlib.pyplot as plt
import numpy as np

from tmkit import TickmateDatabase

matplotlib.style.use('ggplot')

tm = TickmateDatabase(sys.argv[1], hide_names=True)
names = list(tm.tracks.name)

ts = tm.timeseries.copy()
ts.columns = names
print(ts.corr())

fig, ax = plt.subplots()
cax = ax.imshow(np.array(ts.corr()), cmap=plt.cm.hot, interpolation='nearest',
        vmin=-1, vmax=1)
plt.yticks(range(len(names)), names)
plt.xticks(range(len(names)), names, rotation=45)

cbar = fig.colorbar(cax, ticks=[-1, 0, 1], orientation='vertical')
cbar.ax.set_yticklabels(['Negative correlation', 'No correlation', 'High correlation'])

plt.show()

