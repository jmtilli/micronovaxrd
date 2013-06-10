% g is the fitness properties data structure, which contains:
% g.timeperstep
% g.threshold
function E = relchi2fitnessfunction(simul,meas,g)
	indices = find(simul>0 & meas>0);
    simul = simul(indices);
	meas = meas(indices);

     err = zeros(size(meas));

     %simul = simul * g.timeperstep;
     %meas = meas * g.timeperstep;
     %%g.threshold = g.threshold * g.timeperstep;

     err = err + ((meas-simul).^2./meas) .* (meas<g.threshold);
     err = err + ((meas-simul).^2./(meas.^2/g.threshold)) .* (meas>=g.threshold);
     E = (sum(err)/length(meas))^(1/2);
end
