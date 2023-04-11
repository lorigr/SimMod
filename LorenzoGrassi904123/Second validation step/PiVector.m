clear all

A = xlsread('transitions rates matrix', 1, 'B2:AY52');

b = xlsread('transitions rates matrix', 1, 'AZ2:AZ52');

pi = A\b

piT = pi';




