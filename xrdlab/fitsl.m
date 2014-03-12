% import measurement
scan = HRXRDimport('sl.x00');
theta = scan.theta';
meas = scan.meas';
lambda = scan.lambda;

% load material database
matZ = matdb;

% calculate material properties for the applied wavelength
GaAs = Zmat2mat(matZ.GaAs_004, lambda);
InAs = Zmat2mat(matZ.InAs_004, lambda);

% fixed mixtures
GaAs_mixture1 = {1.0, GaAs};
InAs_mixture1 = {1.0, InAs};

% variable mixtures
InGaAs_mixture2.first = GaAs_mixture1;
InGaAs_mixture2.second = InAs_mixture1;
InGaAs_mixture2.x = 0; % will be replaced later with the value from p

GaAs_mixture2.first = GaAs_mixture1;
GaAs_mixture2.second = GaAs_mixture1;
GaAs_mixture2.x = 0; % will be replaced later with the value from p

% layer materials
mixtures{1} = GaAs_mixture2;
mixtures{2} = InGaAs_mixture2;
mixtures{3} = GaAs_mixture2;

% calculate GaAs_xyspace
[dummy,GaAs_xyspace,dummy] = matsusc(GaAs_mixture2, 0);

% mixing vector: the layer structure is
% 1 (GaAs)
% 2 (InGaAs)
% 1 (GaAs)
% 2 (InGaAs)
% 1 (GaAs)
% 2 (InGaAs)
% 3 (substrate)
% layers with the same number are fitted simultaneously
mixing = [1,2,1,2,1,2,3];

% The layers adopt the crystal structure of substrate
% so they have the same in-plane lattice constant.
%
% layer materials are GaAs, InGaAs, GaAs
%
% could be also [GaAs_xyspace, GaAs_xyspace, GaAs_xyspace]
rel = [0,0,1];
rel_min = [0,0,1];
rel_max = [0,0,1];

% no instrument convolution (in radians)
stddevrad = 0.0;

% layer thickness
d_min = [0.0,0.0,0.0,];
d = [1.85E-8,6.9000000000000006E-9,0.0,];
d_max = [3.29E-8,1.59E-8,0.0,];

% composition
p_min = [0.0,0.0,0.0,];
p = [0.0,0.2676,0.0,];
p_max = [0.0,0.500,0.0,];

%p_min = p;
%p_max = p;
%d_min = d;
%d_max = d;

% normalization: full intensity in decibels
prodfactor_min = 52.363001743999995;
prodfactor = 57.037982546239995;
prodfactor_max = 61.0203736;

% systematic error in measurement angle (in radians)
thetaoffset_min = -0.02 * pi/180;
thetaoffset = 0;
thetaoffset_max = 0.02 * pi/180;

% sum term in decibels
% don't fit since background intensity is below photon counting noise
% in this measurement
sumterm_min = -100;
sumterm = -100;
sumterm_max = -100;

% should be 3..10 times the number of effective fitting parameters
% (in this case, 15 .. 50)
npop = 30;
niter = 120;

% initialize fitting
ctx = fitDE_initXRD(theta, meas, mixtures, lambda, stddevrad, rel_min, rel, rel_max, d_min, d, d_max, p_min, p, p_max, prodfactor_min, prodfactor, prodfactor_max, sumterm_min, sumterm, sumterm_max, thetaoffset_min, thetaoffset, thetaoffset_max, mixing, 'CovDE', npop);

for iter=1:niter
    ctx = fitDE(ctx);
    y = fitDE_best(ctx);
    bestfitness = fitDE_best_fitness(ctx);
    medianfitness = fitDE_median_fitness(ctx);
    iter,bestfitness, medianfitness
    prodfactor = y(1);
    sumterm = y(2);
    thetaoffset = y(3);
    y2 = y(4:end);
    d = y2(1:end/3)';
    x = y2(end/3+1:2*end/3);
    rel = y2(2*end/3+1:end);

    prodfactor,sumterm,thetaoffset,x,d,rel


    simul = XRDindividuals(y,ctx.q);

    axis([theta(1), theta(end), log(scan.PhotonLevel*0.5), log(max(meas*2))]);
    plot(theta, log(meas), theta, log(simul));

    drawnow;
end

niter, bestfitness, thetaoffset, medianfitness
prodfactor, sumterm, x, d, rel
