USAGE



1. Measurements



If you want to test the program without using a real measurement, choose
File -> Load empty measurement and enter the desired angular range and the
number of data points. This sets the measurement to 0 dB so you can build
various layer models and compare the simulated curves.

If you want to test fitting to a theoretical curve, load an empty measurement
with an angular range near the Bragg peak, build the layer model and then
select File -> Use simulation as measurement. You might want add noise to the
"measurement" with Data -> Add noise.


2. Layer editor

You can build the layer model on the layer editor tab. New layers can be added
with the add button. If you want to edit a layer which is already on the layer
stack, select the layer and click edit. Multiple layers can be selected by
using ctrl and shift.

If you have selected a contiguous range of layers (or a single layer), you can
move it in the stack by using the move up and move down buttons. Single or
multiple layers may be deleted by using the delete button.

When adding a new layer, you must enter a name describing the layer. Different
layers should have different names. For example, if you have two GaAs layers
with different thicknesses, you should enter different names for them.

The default wavelength is Cu K_alpha, which is 0.154056 nm. The wavelength can
be changed with the edit button. The wavelength is stored in the layer file
along with the layer model and the fitting parameters.

3. Fitting parameters

The three fitting parameters for each layer are thickness, composition and
degree of relaxation. You must specify the minimum and maximum values which
are used as the fitting range for the fitting algorithm. The algorithm starts
by looking for the optimal solution uniformly between the minimum and maximum
values. If you have too small fitting range, the optimal solution might be out
of the fitting range, which makes it unable to find the solution. If you have
too large fitting range, the fit might not converge or if it does, it
converges slowly.

The expected values are the best guesses for the fitting parameters. You
should start by entering the most probable value as the expected value. After
automatic fitting, the expected values have the fitting results. Expected
values are important for convergence: if you have accurate expected values but
a wide fitting range, convergence is guaranteed to happen but it is slow since
the fitting algorithm looks for optimal solutions in the whole fitting range.
If it doesn't find better solutions than the expected value, it directs the
searches more and more near the expected value during every iteration.

A particular fitting parameter can be excluded from fitting by unchecking the
fit checkbox or having equal minimum and maximum values.

The thickness is the layer thickness in nanometers. The composition is the
mole fraction of material 2 in the mixture. For example, if material1 is GaAs
and material2 InAs, then a composition of 0.07 means In_{0.07}Ga_{0.93}As.
Composition must always be between 0 and 1.

The degree of relaxation should be between 0 and 1. A degree of relaxation of
0 means that the layer is fully strained: the in-plane lattice constant is the
same as the in-plane lattice constant of the layer below. The lattice constant
perpendicular to surface is calculated from in-plane strain and Poisson's
ratio. A degree of relaxation 1 means that the lattice constants are the same
as the bulk values. Values between 0 and 1 means that the in-plane lattice
constant is linearly interpolated between these two extremes.

Strain may be calculated only for layers that are on top of another layer of
the same crystal structure. For other layers, the degree of relaxation must be
manually set to 1. Otherwise you will get incorrect results.

For ideal structures the degree of relaxation is 0 for every layer, which
means that all layers have the same in-plane lattice constant determined by
the substrate. The substrate (which is the bottom layer) is always fully
relaxed regardless of the value of the degree of relaxation. The substrate is
always infinitely thick.

Composition and degree of relaxation may not be fitted at the same time. These
fitting parameters have very large correlation, so fitting both at the same
time requires extremely accurate models and measurements. Otherwise you will
get a result, but it is most probably very inaccurate.

The material is a mixture of two materials with a composition that can be
fitted. Both materials must be specified and they must be compatible with each
other -- for example, if you have a GaAs substrate, you must set both
materials to GaAs. In this case the composition doesn't matter, so you can
specify anything.

Compatibility means that both materials have the same crystal structure and
the same miller index of reflection. For example, AlN (0002) and AlN (0004)
aren't compatible with each other and GaAs (400) and GaN (0004) aren't
compatible. AlN (0004) and GaN (0004) are compatible. For GaAsN, you must use
the cubic form of GaN, which is GaN (400).

For more complicated compositions, the two constituents may be mixtures
instead of simple materials. For example, if you have
In_{0.05}Al_{x}Ga_{1-x-0.05}As and you want to fit x, it can be done with
these constituents:
- material 1: mixture of InAs (composition = 0.05) and GaAs (composition = 0.95)
- material 2: mixture of InAs (composition = 0.05) and AlAs (composition = 0.95)

The composition fitting parameter is the molar fraction of the second
material. The molar fractions of the atoms in the final mixture are

  x_{In} = 0.05*(1-c) + 0.05*c = 0.05
  x_{Ga} = 0.95*(1-c) = 0.95 - 0.95c
  x_{Al} = 0.95c

where c is the composition fitting parameter. You don't get the value for x
directly but must calculate it from x = 0.95c.

The materials can be specified by clicking the buttons set simple material and
set mixture.



4. Superlattice support

To aid in fitting complex layer structures, layers in different places may
be linked together to decrease the number of fitting parameters. For example,
consider the following structure:

	GaAs [18.5 nm]
	InGaAs [7 nm, x_{In} = 0.25]
	GaAs [18.5 nm]
	InGaAs [7 nm, x_{In} = 0.25]
	GaAs [18.5 nm]
	InGaAs [7 nm, x_{In} = 0.25]
	GaAs substrate

The exact thicknesses of GaAs and InGaAs layers and the composition of InGaAs
are not known exactly, but it's known that all the GaAs and InGaAs layers have
the same thickness, and that all the InGaAs layers have the same composition.
If you add all the layers independently to the layer model, you would have 9
fitting parameters, which makes fitting extremely hard.

Instead, you can add the two-layer GaAs-InGaAs structure, select both layers
and click duplicate, which makes a linked copy of the selected layers.
Finally, add the substrate to the bottom of the stack. You should have the
following layer stack:

Layer 1 pos 1: GaAs, d = 18.5000 nm (fit), p = 0.00000 (no fit), r = 0.00000 (no fit)
Layer 2 pos 2: InGaAs, d = 7.00000 nm (fit), p = 0.250000 (fit), r = 0.00000 (no fit)
Layer 1 pos 3: GaAs, d = 18.5000 nm (fit), p = 0.00000 (no fit), r = 0.00000 (no fit)
Layer 2 pos 4: InGaAs, d = 7.00000 nm (fit), p = 0.250000 (fit), r = 0.00000 (no fit)
Layer 1 pos 5: GaAs, d = 18.5000 nm (fit), p = 0.00000 (no fit), r = 0.00000 (no fit)
Layer 2 pos 6: InGaAs, d = 7.00000 nm (fit), p = 0.250000 (fit), r = 0.00000 (no fit)
Layer 3 pos 7: GaAs substrate, d = 0.00000 nm (no fit), p = 0.00000 (no fit), r = 0.00000 (no fit)

Layers 1 and 2 occur in 3 different places. All the occurrences use the same
fitting parameters, so the total number of fitting parameters is 3. If you
want to fit all layers separately, you must make the following stack by using
the copy button instead of the duplicate button:

Layer 1 pos 1: GaAs, d = 18.5000 nm (fit), p = 0.00000 (no fit), r = 0.00000 (no fit)
Layer 2 pos 2: InGaAs, d = 7.00000 nm (fit), p = 0.250000 (fit), r = 0.00000 (no fit)
Layer 3 pos 3: GaAs, d = 18.5000 nm (fit), p = 0.00000 (no fit), r = 0.00000 (no fit)
Layer 4 pos 4: InGaAs, d = 7.00000 nm (fit), p = 0.250000 (fit), r = 0.00000 (no fit)
Layer 5 pos 5: GaAs, d = 18.5000 nm (fit), p = 0.00000 (no fit), r = 0.00000 (no fit)
Layer 6 pos 6: InGaAs, d = 7.00000 nm (fit), p = 0.250000 (fit), r = 0.00000 (no fit)
Layer 7 pos 7: GaAs substrate, d = 0.00000 nm (no fit), p = 0.00000 (no fit), r = 0.00000 (no fit)

If you have fitted the first structure with linked layers and want to refine
the fit by fitting all layers independently of the others, you can select the
layers and click separate, which makes all selected layers independent.




5. Manual fit

On the manual fit tab you can adjust fitting parameters to see how they affect
the simulated curve. There are tabs for each layer in the stack and a single
tab for the global parameters. Above the parameters is a logarithmic plot of
the simulated and measured curves. The plot range can be changed with
Data -> Plot range

The global parameters are the sum term (for background radiation),
normalization factor, angle offset and FWHM for instrument convolution. The
normalization factor shifts the curve vertically and offset shifts it
horizontally. At least normalization factor should be fitted. If the substrate
peaks of the simulation and measurement have different angles due to tilting
of the reflecting planes with respect to sample surface, fitting the offset
might help. FWHM can't be fitted.

The sliders have the following structure: first, there is the name of the
parameter, its unit and the value used to calculate the simulated curve.  Then
there is the min button which allows setting the minimum value quickly to the
current value. The minimum value is shown after the min button. Then there is
the slider to adjust the current value between min and max. After the slider
there is the maximum value and the max button to set the maximum value
quickly. You can set the minimum and maximum values (and the current value)
manually by clicking the edit button. You need the edit button eg. when you
need to decrease the minimum value or increase the maximum value. The value
will be fitted only when the fit checkbox is enabled.


For example, the following slider

r = 0.0000 [Min] 0.0000 [Slider] 1.0000 [Max] [Edit...]  [ ]Fit

means that the parameter is r (degree of relaxation) and it is unitless. The
current value is 0 which means that the layer is fully strained. The minimum
value is 0 and the maximum value is 1, so the fitting algorithm would search
for solutions between the two extremes were the fit checkbox enabled. However,
it is disabled so the fitting algorithm will search for solutions in which the
layer is always fully strained.






6. Fitting to a theoretical model

It is possible to fit to a theoretical curve instead of a measured curve. To
do that, select File -> Load empty measurement and set the desired angular
range and the number of data points. Then build the layer model and select
File -> Use simulation as measurement. The measured curve is changed to the
current simulated curve. You can add noise to the theoretical measurement by
selecting Data -> Add noise and entering the photon level. The default photon
level is 0 dB, so if you want to have a theoretical measurement with the full
intensity of 100 000 photons, set the normalization factor to 50 dB before
selecting File -> Use simulation as measurement, and then add the noise with
the default photon level of 0 dB.



7. Automatic fitting

You can use the fitting algorithms in the automatic fit tab. Since automatic
fitting is slow, the layer model in the automatic fit tab is different from
the model in the layer editor and manual fit tabs. This makes it possible to
refine the layer model manually while waiting for the automatic fitting to
complete.

To fit a model automatically, load a measurement and build a layer model with
sensible fitting ranges for the parameters using the layer editor and manual
fit tabs. Then push the import model button on the automatic fit tab. The
model in the layer editor and manual fit tabs is copied to the automatic fit
tab.

Currently only two algorithms are provided: DE and CovDE. DE is differential
evolution, a simple but efficient genetic algorithm variant. CovDE is a DE
which uses the population covariance matrix to transform fitting parameters
during recombination to reduce interparameter correlation.

The population size should be about 3-10 times the number of fitting
parameters. To small population size makes the algorithm fast but increases
the probability of not finding the global optimum. If the population size is
too large, the probability of finding the global optimum is high but the
algorithm converges extremely slowly.

The number of iterations should be set to a large value like 1000. Usually
100-200 iterations are enough, but the required number of iterations depends
on the number and ranges of fitting parameters.

You can start fitting by clicking the start fit button. The simulated curve on
the automatic fit tab is updated as the fit converges. The simulated curve is
the best fit. The fitnesses of the best fit and the median fit are shown above
the plotted curves. If the median fitness reaches the best fitness and stays
on the same level for a long time, the fitting has converged and you can stop
the fitting by clicking the stop fit button.


Once the maximum number of iterations is reached or you clicked the stop fit
button, the fitting stops. You can export the fitting results to the manual
fit tab by clicking the export model button. After you have exported the
results, they may be saved to a layer file by choosing File -> Save layers.
When saving the layer model, the model on the layer editor and manual fit tabs
is always used, so remember to export the model to these tabs after fitting if
you want to save the fitting results, not the initial layer model.
