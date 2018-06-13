import numpy as np
import matplotlib.pyplot as pyplot


def reduce_buckets(array):
    keys = np.unique(array[:, 0])
    result = np.zeros((keys.shape[0], 2), dtype=int)
    index = 0
    for k in keys:
        entries = array[np.where(array[:, 0] == k)]
        sum = np.sum(entries[:, 1])
        result[index] = [k, sum]
        index += 1

    return result


distance_csv = np.genfromtxt('./data/distance.csv', delimiter=',', dtype=np.int)
distance_buckets = reduce_buckets(distance_csv)
fig = pyplot.figure()
pyplot.bar(distance_buckets[:,0], distance_buckets[:,1])
pyplot.xlabel("Distance bucket")
pyplot.ylabel("Number of updates")
pyplot.savefig("distance.png", dpi=fig.dpi)

angle_csv = np.genfromtxt('./data/angle.csv', delimiter=',', dtype=np.int)
angle_buckets = reduce_buckets(angle_csv)
fig = pyplot.figure()
pyplot.bar(angle_buckets[:,0], angle_buckets[:,1])
pyplot.xlabel("Angle bucket")
pyplot.ylabel("Number of updates")
pyplot.savefig("angle.png", dpi=fig.dpi)

energy_csv = np.genfromtxt('./data/energy.csv', delimiter=',', dtype=np.int)
energy_buckets = reduce_buckets(energy_csv)
fig = pyplot.figure()
pyplot.bar(energy_buckets[:,0], energy_buckets[:,1])
pyplot.xlabel("Energy bucket")
pyplot.ylabel("Number of updates")
pyplot.savefig("energy.png", dpi=fig.dpi)


enemyEnergy_csv = np.genfromtxt('./data/enemyEnergy.csv', delimiter=',', dtype=np.int)
enemyEnergy_buckets = reduce_buckets(enemyEnergy_csv)
fig = pyplot.figure()
pyplot.bar(enemyEnergy_buckets[:,0], enemyEnergy_buckets[:,1])
pyplot.xlabel("Enemy energy bucket")
pyplot.ylabel("Number of updates")
pyplot.savefig("enemyEnergy.png", dpi=fig.dpi)


action_csv = np.genfromtxt('./data/action.csv', delimiter=',', dtype=np.int)
action_buckets = reduce_buckets(action_csv)
fig = pyplot.figure()
pyplot.bar(action_buckets[:,0], action_buckets[:,1])
pyplot.xlabel("Action bucket")
pyplot.ylabel("Number of updates")
pyplot.savefig("action.png", dpi=fig.dpi)



