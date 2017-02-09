import tensorflow as tf
import numpy as np
import requests
import json
import time
# Changing data set. Because we use 8 data set so

def xavier_init(n_inputs, n_outputs, uniform=True):
    if uniform:
        # 6 was used in the paper.
        init_range = tf.sqrt(6.0 / (n_inputs + n_outputs))
        return tf.random_uniform_initializer(-init_range, init_range)
    else:
        # 3 gives us approximately the same limits as above since this repicks
        # values greater than 2 standard deviations from the mean.
        stddev = tf.sqrt(3.0 / (n_inputs + n_outputs))
        return tf.truncated_normal_initializer(stddev=stddev)


learning_rate = 0.001

xy = np.loadtxt('training.txt', unpack=True, dtype='float32')
testset = np.loadtxt('testAccuracy.txt', unpack=True, dtype='float32')

x_data = np.transpose(xy[0:6])
y_data = np.transpose(xy[6:])


test_x_data = np.transpose(testset[0:6])
test_y_data = np.transpose(testset[6:])

print('x_data :', x_data.shape)
print('y_data :', y_data.shape)

X = tf.placeholder("float", [None, 6])
Y = tf.placeholder("float", [None, 3])

#W = tf.Variable(tf.zeros([8, 5]))
#hypothesis = tf.nn.softmax(tf.matmul(X, W))
#cost = tf.reduce_mean(-tf.reduce_sum(Y * tf.log(hypothesis), reduction_indices=1))
#optimizer = tf.train.GradientDescentOptimizer(learning_rate).minimize(cost)


W1 = tf.get_variable("W1", shape=[6, 12], initializer=xavier_init(6, 12))
W2 = tf.get_variable("W2", shape=[12, 12], initializer=xavier_init(12, 12))
W3 = tf.get_variable("W3", shape=[12, 12], initializer=xavier_init(12, 12))
W4 = tf.get_variable("W4", shape=[12, 3], initializer=xavier_init(12, 3))


B1 = tf.Variable(tf.random_normal([12]))
B2 = tf.Variable(tf.random_normal([12]))
B3 = tf.Variable(tf.random_normal([12]))
B4 = tf.Variable(tf.random_normal([3]))

L1 = tf.nn.relu(tf.add(tf.matmul(X, W1), B1))
L2 = tf.nn.relu(tf.add(tf.matmul(L1, W2), B2)) # Hidden layer
L3 = tf.nn.relu(tf.add(tf.matmul(L2, W3),B3)) # Hidden layer

hypothesis = tf.add(tf.matmul(L3, W4), B4)
cost = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(hypothesis, Y))
optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate).minimize(cost)

init = tf.initialize_all_variables()

with tf.Session() as sess:
    sess.run(init)

    for step in range(20000):
        sess.run(optimizer, feed_dict={X: x_data, Y: y_data})
        if step % 200 == 0:
            feed = {X: x_data, Y: y_data}
            print  (step ,sess.run(cost , feed_dict={X:x_data , Y:y_data}))

    print('-------------------------------')

    # r = requests.get("http://pesang72.cafe24.com/sensor/getrealdata.php")
    # dict = json.loads(r.text)
    # print(r.text)
    # print(dict['data'][0]['x'])

#    try:
#        while True:
#            r = requests.get("http://pesang72.cafe24.com/sensor/getrealdata.php")
#            dict = json.loads(r.text)
#            print(r.text)
#            print(dict['data'])


#            a = sess.run(hypothesis, feed_dict={X: [[dict['data'][0]['x'], dict['data'][0]['y'],dict['data'][0]['z'], dict['data'][0]['gx'], dict['data'][0]['gy'],dict['data'][0]['gz']]]})
#            print "a :", a, sess.run(tf.arg_max(a, 1))
#            if np.argmax(a) == 0:
#                print "sitdown"
#            elif np.argmax(a) == 1:
#                print "Run"
#            elif np.argmax(a) == 2:
#                print "attact buttom"

#            payload = {'RS': np.argmax(a)}
#            r = requests.get("http://pesang72.cafe24.com/sensor/setstate.php", params=payload)

#            time.sleep(2)
#    except KeyboardInterrupt:
#        print "break while loop"




    # Test model
    correct_prediction = tf.equal(tf.argmax(hypothesis, 1), tf.argmax(Y, 1))
    # Calculate accuracy
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
    print("Accuracy:", accuracy.eval({X: test_x_data, Y: test_y_data}))


    # a = sess.run(hypothesis, feed_dict={X: [[0.0815,0.7859, -0.3516, -0.0493, -0.0448, -0.0393]]})
    # print "a :", a, sess.run(tf.arg_max(a, 1))
    # if np.argmax(a) == 0:
    #     print "sitdown"
    # elif np.argmax(a) == 1:
    #     print "Run"
    #
    # print('-------------------------------')

    # a = sess.run(hypothesis, feed_dict={X: [[0.0547 ,1.0813, 0.4373 ,0.0036 ,-0.0115, -0.0362]]})
    # print "a :", a, sess.run(tf.arg_max(a, 1))
    # if np.argmax(a) == 0:
    #     print "sit down"
    # elif np.argmax(a) == 1:
    #     print "Run"
#    print sess.run(tf.argmax(a, 1))