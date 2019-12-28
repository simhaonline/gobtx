import time

import redis


def job():
    print("I'm working...")
    r = redis.Redis(host='localhost', port=6379, db=0)
    print r


# schedule.every(10).minutes.do(job)
# schedule.every().hour.do(job)
# schedule.every().day.at("10:30").do(job)
# schedule.every().monday.do(job)
# schedule.every().wednesday.at("13:15").do(job)
# schedule.every().minute.at(":17").do(job)

while True:
    job()
    time.sleep(10000)
