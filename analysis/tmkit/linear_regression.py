import sqlite3
from sklearn import linear_model
import numpy as np
import pandas as pd
import datetime
import sys

conn = sqlite3.connect(sys.argv[1])

c = conn.cursor();

c.execute("select _id, name from tracks")

rows = c.fetchall()
track_names = pd.DataFrame([{'track_name': row[1]} for row in rows])
track_ids = [int(row[0]) for row in rows]
track_cnt = len(track_ids)

print "Found {0} tracks.".format(track_cnt)

c.execute("select * from ticks")
last_tick = c.fetchall()[-1]
last_day = datetime.date(last_tick[2], last_tick[3], last_tick[4])

def window(day, n=20):
    "return a matrix of the last `n` days before day `day`"
    tick_date = "date(year || '-' || substr('0' || month, -2, 2) || " + \
                    "'-' || substr('0' || day, -2, 2))"
    max_date = "date('{d.year:04d}-{d.month:02d}-{d.day:02d}')".\
            format(d=day)
    min_date = "date('{d.year:04d}-{d.month:02d}-{d.day:02d}')".\
            format(d=day-datetime.timedelta(n))
    c.execute("select * from ticks where {d} <= {max_date} and {d} >= {min_date}".\
            format(d=tick_date, max_date=max_date, min_date=min_date))
    # ticktrix is the matrix containing the ticks
    ticktrix = np.zeros((n, track_cnt))
    for row in c.fetchall():
        print row
        try:
            row_date = datetime.date(row[2], row[3], row[4])
        except ValueError:
            print "Error constructing date from", row
        x = -(row_date - day).days
        y = track_ids.index(int(row[1]))
        if x < n:
            ticktrix[x, y] = 1
    return ticktrix

last_day -= datetime.timedelta(1)
print "Fitting for day:", last_day

my_window = window(last_day)

target_data = my_window[0,:].T
training_data = my_window[1:,:].T

print "Target:", target_data.shape
print target_data
print "Training:", training_data.shape
print training_data

reg = linear_model.LinearRegression()
reg.fit(training_data, target_data)
print "Coefficents:", reg.coef_.shape
print reg.coef_

print "Applied to training data:"
print np.dot(training_data, reg.coef_)
print "Forecast"
#print np.dot(my_window[:19,:].T, reg.coef_)
#print track_names

df = pd.DataFrame()
df['track'] = track_names
df['prob'] = pd.Series(np.dot(my_window[:19,:].T, reg.coef_) * 100.0)
print df
