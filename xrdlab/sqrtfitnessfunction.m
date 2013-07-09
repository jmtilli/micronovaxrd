% Do the fitness function calculation for one
% measured and one simulated dataset using p-norm
% in sqrt-space. The scaling factor is
% automatically adjusted so that both datasets
% have the same arithmetic means.

% here dataset1 is simul and dataset2 is meas

% g is the fitness properties data structure, which contains:
% g.pnorm:
% ...
function E = sqrtfitnessfunction(dataset1,dataset2,g)
	%indices = find(dataset1>0 & dataset2>0);
  %dataset1 = dataset1(indices);
	%dataset2 = dataset2(indices);

%     %% exchange measured and simulated curves
%     tmp = dataset1; dataset1 = dataset2; dataset2 = tmp;
%     %% now dataset1 is meas and dataset2 is simul
% 
% 
%     % % dataset1 is meas, dataset2 is simul
%     err = 0*dataset1;
%     % % test: photon level is 0.5 in sl.x00
%     dataset1 = dataset1 * 2;
%     dataset2 = dataset2 * 2;
%     threshold = 400;
%     %threshold = 1e4;
%     %threshold = 1e9;
%
%     err = err + ((dataset1-dataset2).^2./dataset1) .* (dataset1<threshold);
%     err = err + ((dataset1-dataset2).^2./(dataset1.^2/threshold)) .* (dataset1>=threshold);
%     E = (sum(err)/length(dataset1))^(1/2);


    % ------------

	a = sqrt(dataset1);
	b = sqrt(dataset2);
	E = norm(a-b, g.pnorm)/length(a)^(1/g.pnorm);
end
