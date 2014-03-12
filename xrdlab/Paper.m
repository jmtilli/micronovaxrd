lambda = 1.540562e-10;

matZ = matdb;
GaAs = Zmat2mat(matZ.GaAs_004,lambda);
AlAs = Zmat2mat(matZ.AlAs_004,lambda);
InAs = Zmat2mat(matZ.InAs_004,lambda);

GaAs_mixture = {1.0, GaAs};
GaAs_mixture2.first = GaAs_mixture;
GaAs_mixture2.second = GaAs_mixture;
GaAs_mixture2.x = 0;

InAs_mixture = {1.0, InAs};
InAs_mixture2.first = InAs_mixture;
InAs_mixture2.second = InAs_mixture;
InAs_mixture2.x = 0;

AlAs_mixture = {1.0, AlAs};
AlAs_mixture2.first = AlAs_mixture;
AlAs_mixture2.second = AlAs_mixture;
AlAs_mixture2.x = 0;

AlGaAs_mixture2.first = AlAs_mixture;
AlGaAs_mixture2.second = GaAs_mixture;
AlGaAs_mixture2.x = 0;







format long

% not strained
[GaAs_suscdata,GaAs_xyspace,GaAs_zspace] = matsusc(GaAs_mixture2, 0);

AlGaAs_mixture2.x = 0.5;
[AlGaAs_suscdata,AlGaAs_xyspace,AlGaAs_zspace] = matsusc(AlGaAs_mixture2, GaAs_xyspace)


AlGaAs_xyspace^2*AlGaAs_zspace*4
