function E = chi2fitnessfunction(simul,meas,g)
	indices = find(simul>0 & meas>0);
    simul = simul(indices);
	meas = meas(indices);

     err = zeros(size(meas));

     %simul = simul * g.timeperstep;
     %meas = meas * g.timeperstep;

     err = err + ((meas-simul).^2./meas);
     E = (sum(err)/length(meas))^(1/2);
end
