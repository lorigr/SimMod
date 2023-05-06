clear all

format long

A = xlsread('transitions rates matrix', 1, 'B2:AY51');

A = A';

for i =1: 50
    A(i,i)=-(sum(A(:,i))-A(i,i));
end    

lastRow = ones(1,50);

A(51,:) = lastRow;

b = xlsread('transitions rates matrix', 1, 'AZ2:AZ52');

pi = A\b;

piT = pi'

%xlswrite("transitions rates matrix copia.xlsx", piT, 1, 'B55');