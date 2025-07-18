package autonomousplane.simulation.console.commands;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private static BundleContext context;
	protected ServiceRegistration<?> commandProvReg = null;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "AutonomousPlane");
		props.put("osgi.command.function", new String[] { 
				
				//
				// CONFIGURACIÓN 
				//
				
					//  configure : realiza una configuración inicial de servicios y 
					//    dispositivos (usa la sonda Initial-System-Configuration)
					//	  para provocar una configuración inicial
					"initialize",
					"configure",
					
					//  know : muestra las propiedades del knowledge
					"knowledge",
					
					//   show : muestra la configuración actual de conducción
					//
					//   Modo uso
					//		configure
					//
					"show", 						
				
					//
				// SIMULACIÓN LECTURAS DE SENSORES
				//
					//  AOA : sensores de angulo de ataque (AOASensor)
					//
					//	Modo uso
					//		AOA [ set ]  [ double ]
					//		AOA [ get ]	
					//  Ejemplo: detección de línea derecha de carril
					//		AOA get
					"AOA", 
					

				//	
				// PARÁMETROS DE CONTEXTO
				//	
				    // terrain-altitude : sensor de altitud del terreno (RadioAltimeterSensor)
					//
					//  Modo uso
					//		terrain-altitude [ set ]  [ double ]
					//		terrain-altitude [ get ]
					//  Permite aumentarse o disminuir la altitud del terreno y consultaral
					"terrainAltitude",
					
					// destination : establece el destino de navegación del sistema GNSS
				    //
				    //  Modo uso
				    //      destination [ String ]
				    //
				    //  Permite configurar el destino de vuelo del sistema de navegación (INavigationSystem).
				    //  Al establecer un destino reconocido, se asigna automáticamente la distancia total a recorrer.
				    //  Actualmente solo está soportado: "Castellon" (65 km).
				    //
				    //  Ejemplo:
				    //      destination Castellon
					"destination",
					// climate : establece las condiciones climáticas generales en el entorno de simulación
					//
					//  Modo uso
					//					      climate [ String ]
					//
					//  Permite modificar el clima actual del sistema de sensores meteorológicos (IWeatherSensor).
					//  Al establecer un clima, se actualiza automáticamente la velocidad del viento asociada a esa condición.
					//
					//  Valores soportados:
					//					      - "Clear"    → Cielo despejado, viento suave (10 km/h).
					//					      - "Stormy"   → Tormenta, viento fuerte (70 km/h).
					//
					//  Ejemplo:
					//					      climate Stormy
					"climate",
					// wind : configura manualmente las condiciones del viento
					//
					//  Modo de uso:
					//					      wind speed [ double ]        → Establece la velocidad del viento en km/h.
					//					      wind direction [ double ]    → Establece la dirección del viento en grados.
					//
					//  Permite ajustar directamente la velocidad y dirección del viento en el sistema de sensores meteorológicos (IWeatherSensor).
					//  Estos valores sobrescriben cualquier configuración previa de clima.
					//
					//  Ejemplos:
					//					      wind speed 35.0
					//					      wind direction 270.0
					"wind",
					// groundTemperature : establece o consulta la temperatura en superficie
					//
					//  Modo uso
					//		 groundTemperature set [ double ]
					//		 groundTemperature get
					//
					//  Permite leer o modificar la temperatura del terreno registrada por el sensor climático (IWeatherSensor).
					//  El valor de temperatura debe proporcionarse en grados Celsius cuando se usa el modo "set".
					//
					//  Ejemplos:
					//					      groundTemperature set 22.5
					//					      groundTemperature get
					"groundTemperature",// sensor de clima (WeatherSensor)
					
				//	
				// CONTROL MANUAL DEL VEHÍCULO
				//	
					// ControlSurface : sensores de superficie de control (ControlSurfaceSensor)
					//
					//  Modo uso
					//		ControlSurface [ set ] [String] [ double ]
					// Ejemplo: set Aileron 10.0
					"controlSurface",
					// thrust : acelera el avion mdoificando un porcentaje de empuje
					//
					//  Modo uso
					//		acceleration [ set ] [String] [ double ]
					//		acceleration [ get ] [String]
					// Ejemplo: acceleration 10
					"thrust", 
					// speed : acelera el avion seleccionando una velocidad km/h
					//
					//  Modo uso
					//		speed [ set ] [ double ]
					//		speed [ get ] 
					// Ejemplo: speed set 100
					"speed",
					// ascend : inicia una maniobra de ascenso ajustando el ángulo de pitch y la velocidad objetivo.
					//
					// Modo de uso:
					//		ascend [ slow | middle | fast ]
					//
					// Ejemplos:
					//	  ascend slow     → Pitch suave (~5°) y velocidad reducida (~400 km/h)
					//	  ascend middle   → Pitch medio (~10°) y velocidad media (~600 km/h)
					//	  ascend fast     → Pitch pronunciado (~15°) y velocidad alta (~750 km/h)
					"ascend",
					// descend : inicia una maniobra de descenso ajustando el ángulo de pitch negativo y la velocidad objetivo.
					//
					// Modo de uso:
					//		  descend [ slow | middle | fast ]
					//
				    // Ejemplos:
					//	      descend slow     → Pitch negativo suave (~-3°) y velocidad moderada (~500 km/h)
					//		  descend middle   → Pitch negativo medio (~-7°) y velocidad media (~600 km/h)
					//		  descend fast     → Pitch negativo pronunciado (~-12°) y velocidad alta (~700 km/h)

					"descend",
					
					
				//	
				// FUNCIONES DE CONDUCCIÓN
				//	

					// driving : activa un nivel de vuelo autónoma
					//
					//  Modo uso
					//		driving [ l0 | l1 | l2 | l3 ]
					//
					//  Ejemplo: Activar nivel de autonomia 3
					//		driving l3
					//      * NOTA: en función de las condiciones se activara el tipo 
					//		  de servicio de vuelo adecuado para el avión.
					//
					//
					"flying",
					
				//
				// SIMULADOR
				//
					//  time-step : establece el paso de tiempo de simulación (define cuantos segundos tiene cada paso de simulación)
					//
					// Modo uso
					//		time-step [ double ]
					// Ejemplo: time-step 1
					
					"timeStep", // establece el paso de tiempo de simulación
					// next ó n : da un paso de simulación manual
					//
					//  Modo uso
					//		next
					//		n
					//
					"next",
					"n"
		});
		System.out.println("Ámbito de comandos: " + props.get("osgi.command.scope"));
		System.out.println("Funciones de comandos: " + props.get("osgi.command.function"));

		this.commandProvReg = context.registerService(MyCommandProvider.class.getName(),
				new MyCommandProvider(context), props);

		
	}

	public void stop(BundleContext bundleContext) throws Exception {
		if ( this.commandProvReg != null )
			this.commandProvReg.unregister();
		
		Activator.context = null;
	}


}